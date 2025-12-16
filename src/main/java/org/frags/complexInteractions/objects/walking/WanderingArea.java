package org.frags.complexInteractions.objects.walking;

import com.destroystokyo.paper.entity.Pathfinder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WanderingArea {

    private final Random random = new Random();

    private World world;
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;
    private List<BoundingBox> forbiddenZones;

    public WanderingArea(Location firstLocation, Location secondLocation) {
        if (!firstLocation.getWorld().equals(secondLocation.getWorld())) {
            throw new IllegalArgumentException("Las dos ubicaciones deben estar en el mismo mundo");
        }

        this.world = firstLocation.getWorld();

        this.minX = Math.min(firstLocation.getX(), secondLocation.getX());
        this.minY = Math.min(firstLocation.getY(), secondLocation.getY());
        this.minZ = Math.min(firstLocation.getZ(), secondLocation.getZ());

        this.maxX = Math.max(firstLocation.getX(), secondLocation.getX());
        this.maxY = Math.max(firstLocation.getY(), secondLocation.getY());
        this.maxZ = Math.max(firstLocation.getZ(), secondLocation.getZ());

        this.forbiddenZones = new ArrayList<>();
    }

    public void addForbiddenZone(Location pos1, Location pos2) {
        if (!pos1.getWorld().equals(world)) return;
        this.forbiddenZones.add(BoundingBox.of(pos1, pos2));
    }

    public void addForbiddenZone(BoundingBox box) {
        this.forbiddenZones.add(box);
    }

    public boolean isPointValid(Location location) {
        if (!location.getWorld().equals(this.world)) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        if (!(x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ)) return false;

        for (BoundingBox box : this.forbiddenZones) {
            if (box.contains(location.toVector())) return false;
        }

        return true;
    }

    public Location getMinLocation() {
        return new Location(world, minX, minY, minZ);
    }

    public Location getMaxLocation() {
        return new Location(world, maxX, maxY, maxZ);
    }

    public boolean isPathSafe(Pathfinder.PathResult path) {
        return getBlockingZone(path) == null;
    }


    public List<BoundingBox> getForbiddenZones() {
        return forbiddenZones;
    }

    public BoundingBox getBlockingZone(Pathfinder.PathResult path) {
        if (path == null) return null;

        for (Location point : path.getPoints()) {
            Vector ptVect = point.toVector();
            for (BoundingBox forbidden : forbiddenZones) {
                if (forbidden.contains(ptVect)) return forbidden;
            }
        }

        return null;
    }

    public List<Location> getCorners(BoundingBox box) {
        List<Location> corners = new ArrayList<>();
        double margin = 2.0;

        double minX = box.getMinX() - margin;
        double minZ = box.getMinZ() - margin;
        double maxX = box.getMaxX() + margin;
        double maxZ = box.getMaxZ() + margin;

        double y = box.getMinY();

        corners.add(new Location(world, minX, y, minZ));
        corners.add(new Location(world, minX, y, maxZ));
        corners.add(new Location(world, maxX, y, minZ));
        corners.add(new Location(world, maxX, y, maxZ));

        return corners;
    }

    public Location getRandomLocation() {
        for (int i = 0; i < 10; i++) {

            double x = minX + (random.nextDouble() * (maxX - minX));
            double z = minZ + (random.nextDouble() * (maxZ - minZ));

            for (int y = (int) maxY; y >= (int) (minY -1); y--) {

                Block feetBlock = world.getBlockAt((int) x, y, (int) z);

                if (!feetBlock.isPassable()) {

                    Block headBlock = feetBlock.getRelative(0, 1, 0);
                    Block bodyBlock = feetBlock.getRelative(0, 2, 0);

                    if (headBlock.isPassable() && bodyBlock.isPassable()) {
                        Location location = new Location(world, x, y + 1, z);
                        if (isPointValid(location)) {
                            return location;
                        }
                    }
                }
            }
        }

        return null;
    }
}
