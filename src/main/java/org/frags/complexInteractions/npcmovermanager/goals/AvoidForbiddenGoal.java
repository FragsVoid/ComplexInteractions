package org.frags.complexInteractions.npcmovermanager.goals;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Mob;
import org.bukkit.util.BoundingBox;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.walking.WalkingObject;
import org.frags.complexInteractions.objects.walking.WanderingArea;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class AvoidForbiddenGoal implements Goal<Chicken> {

    private final GoalKey<Chicken> key;
    private final Mob mob;
    private final WalkingObject walkingObject;
    private final Random random = new Random();

    private Pathfinder.PathResult activePath;
    private boolean isDetouring = false;

    private long lastCalculationTime = 0;

    public AvoidForbiddenGoal(ComplexInteractions plugin, Mob mob, WalkingObject walkingObject) {
        this.key = GoalKey.of(Chicken.class, new NamespacedKey(plugin, "avoid_forbidden_zone"));
        this.mob = mob;
        this.walkingObject = walkingObject;
    }

    @Override
    public boolean shouldActivate() {
        if (System.currentTimeMillis() - lastCalculationTime < 1000) return false;
        lastCalculationTime = System.currentTimeMillis();
        if (mob.getPathfinder().hasPath()) return false;

        Location finalDest = walkingObject.getWanderingArea().getRandomLocation();
        if (finalDest == null) return false;

        this.activePath = calculatePath(finalDest);

        return activePath != null;
    }

    @Override
    public void start() {
        if (activePath != null) {
            mob.getPathfinder().moveTo(activePath);
        }
    }

    @Override
    public void stop() {
        activePath = null;
        isDetouring = false;
    }

    @Override
    public @NotNull GoalKey<Chicken> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }

    private Pathfinder.PathResult calculatePath(Location target) {
        WanderingArea area = walkingObject.getWanderingArea();

        Pathfinder.PathResult directPath = mob.getPathfinder().findPath(target);

        if (directPath != null && area.isPathSafe(directPath)) {
            return directPath;
        }

        BoundingBox blockingZone = area.getBlockingZone(directPath);
        if (blockingZone == null) return null;

        List<Location> corners = area.getCorners(blockingZone);

        Pathfinder.PathResult bestDetourPath = null;
        double shortestDistanceToTarget = Double.MAX_VALUE;

        for (Location corner : corners) {
            corner.setY(mob.getLocation().getY());

            Pathfinder.PathResult pathToCorner = mob.getPathfinder().findPath(corner);

            if (pathToCorner != null && area.isPathSafe(pathToCorner)) {

                double distToCorner = mob.getLocation().distanceSquared(corner);
                double distCornerToTarget = corner.distanceSquared(target);
                double totalScore = distToCorner + distCornerToTarget;
                if (totalScore < shortestDistanceToTarget) {
                    shortestDistanceToTarget = totalScore;
                    bestDetourPath = pathToCorner;
                }
            }
        }

        return bestDetourPath;
    }
}
