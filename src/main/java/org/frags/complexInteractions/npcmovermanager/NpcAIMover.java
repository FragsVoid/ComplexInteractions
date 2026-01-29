package org.frags.complexInteractions.npcmovermanager;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.npcmovermanager.goals.AvoidForbiddenGoal;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;
import org.frags.complexInteractions.objects.walking.WalkingMode;
import org.frags.complexInteractions.objects.walking.WalkingObject;
import org.frags.complexInteractions.objects.walking.Waypoints;

import java.util.*;

public class NpcAIMover {

    private ComplexInteractions plugin;

    private Map<String, ActiveNpcContext> activeTasks = new HashMap<>();

    private final Random rand = new Random();

    private NpcAdapter npcAdapter;

    public NpcAIMover(ComplexInteractions plugin, NpcAdapter npcAdapter) {
        this.plugin = plugin;
        this.npcAdapter = npcAdapter;
    }

    public void startWalkingTask(String npcId) {
        if (activeTasks.containsKey(npcId)) {
            ActiveNpcContext context = activeTasks.get(npcId);

            boolean isGuideValid = context.guide != null && context.guide.isValid() && !context.guide.isDead();
            boolean isTaskRunning = context.activeTask != null && !context.activeTask.isCancelled();

            if (isGuideValid && isTaskRunning) {
                return;
            } else {
                cancelTask(npcId);
            }
        }

        if (!npcAdapter.isValid(plugin, npcId) || activeTasks.containsKey(npcId)) return;

        Location start = npcAdapter.getLocation(plugin, npcId);

        removeOldGuides(start, npcId);

        Chicken guide = start.getWorld().spawn(start, Chicken.class, v -> {
            v.setPersistent(true);
            v.setRemoveWhenFarAway(false);

            v.setInvulnerable(true);
            v.setSilent(true);
            v.setCollidable(false);
            v.setInvisible(true);
            v.addScoreboardTag("npc_villager_interactions");
            v.addScoreboardTag("guide_npc_" + npcId);

            v.setAI(true);
        });

        WalkingObject walkingObject = plugin.getWalkingManager().getWalkingByNpcId(npcId);

        Bukkit.getMobGoals().removeAllGoals(guide);
        Bukkit.getMobGoals().addGoal(guide, 2, new AvoidForbiddenGoal(plugin, guide, walkingObject));

        new BukkitRunnable() {
            int attempts = 0;

            @Override
            public void run() {
                attempts++;
                if (!guide.isValid() || attempts > 10) {
                    guide.remove();
                    cancel();
                    return;
                }

                if (guide.isOnGround()) {
                    startMoving(npcId, walkingObject, guide);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 5L, 2L);
    }

    private void removeOldGuides(Location location, String npcId) {
        String targetTag = "guide_npc_" + npcId;

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 50, 50, 50);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Chicken) {
                if (entity.getScoreboardTags().contains(targetTag)) {
                    entity.remove();
                }
            }
        }
    }

    public void walkGhost(Npc ghostNpc, String originalNpcId, List<Location> path, Runnable onComplete) {
        if (path == null || path.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        String taskId = ghostNpc.getData().getId();
        cancelTask(taskId);

        Location start = ghostNpc.getData().getLocation();
        removeOldGuides(start, taskId);

        Zombie guide = createGuideZombie(start, taskId);

        new BukkitRunnable() {
            int attempts = 0;
            @Override
            public void run() {
                attempts++;
                if (!guide.isValid() || attempts > 10) {
                    guide.remove();
                    cancel();
                    return;
                }

                if (guide.isOnGround()) {
                    startPathMoving(taskId, ghostNpc, originalNpcId, guide, path, onComplete);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private void startPathMoving(String taskId, Npc ghostNpc, String originalNpcId, Zombie guide, List<Location> path, Runnable onComplete) {

        guide.getPathfinder().moveTo(path.getFirst());

        BukkitTask task = new BukkitRunnable() {
            int currentTargetIndex = 0;
            int stuckTimer= 0;
            @Override
            public void run() {
                if (isCancelled() || !guide.isValid() || guide.isDead()) {
                    cleanup();
                    return;
                }
                stuckTimer++;

                Location currentLoc = guide.getLocation();
                plugin.getNpcAdapter().updateNpcInstanceLocation(ghostNpc, currentLoc);

                Location target = path.get(currentTargetIndex);

                if (!currentLoc.getWorld().equals(target.getWorld())) {
                    guide.teleport(target);
                    currentLoc = target;
                }

                if (currentLoc.distanceSquared(target) < 1.25 || stuckTimer > 1000) {
                    if (stuckTimer > 1000) {
                        guide.teleport(target);
                    }
                    currentTargetIndex++;
                    stuckTimer = 0;

                    if (currentTargetIndex >= path.size()) {
                        if (onComplete != null) onComplete.run();
                        cleanup();
                        return;
                    }

                    guide.getPathfinder().moveTo(path.get(currentTargetIndex), 0.8);
                }

                if (!guide.getPathfinder().hasPath()) {
                    guide.getPathfinder().moveTo(path.get(currentTargetIndex), 0.8);
                }
            }

            private void cleanup() {
                activeTasks.remove(taskId);
                guide.remove();
                cancel();
            }

        }.runTaskTimer(plugin, 0L, 1L);

        activeTasks.put(taskId, new ActiveNpcContext(guide, task));
    }

    public void walkTo(String npcId,  Location target, Runnable onComplete) {
        cancelTask(npcId);

        if (npcAdapter.isValid(plugin, npcId)) return;

        Location start = npcAdapter.getLocation(plugin, npcId);
        removeOldGuides(start, npcId);

        Chicken guide = createGuide(start, npcId);

        new BukkitRunnable() {
            int attempts = 0;
            @Override
            public void run() {
                attempts++;
                if (!guide.isValid() || attempts > 10) {
                    guide.remove();
                    cancel();
                    return;
                }

                if (guide.isOnGround()) {
                    startDirectedMove(npcId, guide, target, onComplete);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private void startDirectedMove(String npcId, Chicken guide, Location target, Runnable onComplete) {
        guide.getPathfinder().moveTo(target);

        BukkitTask task = new BukkitRunnable() {
            Location lastLocation = null;
            int stuckCounter = 0;
            @Override
            public void run() {
                if (isCancelled() || !guide.isValid() || guide.isDead()) {
                    activeTasks.remove(npcId);
                    if (guide.isValid()) guide.remove();
                    cancel();
                    return;
                }

                Location currentLoc = guide.getLocation();

                npcAdapter.updateNpcLocation(plugin, npcId, currentLoc);

                if (currentLoc.getWorld().equals(target.getWorld()) && currentLoc.distanceSquared(target) < 0.5) {
                    if (onComplete != null) onComplete.run();
                    cleanup();
                    return;
                }

                if (!guide.getPathfinder().hasPath()) {
                    boolean canMove = guide.getPathfinder().moveTo(target);
                    if (!canMove) {
                        cleanup();
                    }
                }
            }

            private void cleanup() {
                activeTasks.remove(npcId);
                guide.remove();
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);

        activeTasks.put(npcId, new ActiveNpcContext(guide, task));
    }

    private Zombie createGuideZombie(Location start, String npcId) {
        return start.getWorld().spawn(start, Zombie.class, v -> {
            v.setPersistent(true);
            v.setRemoveWhenFarAway(false);
            v.setInvulnerable(true);
            v.setSilent(true);
            v.setCollidable(false);
            v.setInvisible(true);

            v.setBaby(true);
            v.setShouldBurnInDay(false);

            v.getEquipment().clear();

            v.addScoreboardTag("npc_villager_interactions");
            v.addScoreboardTag("guide_npc_" + npcId);

            v.setAI(true);

            Bukkit.getMobGoals().removeAllGoals(v);
        });
    }

    private Chicken createGuide(Location start, String npcId) {
        return start.getWorld().spawn(start, Chicken.class, v -> {
            v.setPersistent(true);
            v.setRemoveWhenFarAway(false);
            v.setInvulnerable(true);
            v.setSilent(true);
            v.setCollidable(false);
            v.setInvisible(true);
            v.addScoreboardTag("npc_villager_interactions");
            v.addScoreboardTag("guide_npc_" + npcId);
            v.setAI(true);

            Bukkit.getMobGoals().removeAllGoals(v);
        });
    }

    private void startMoving(String npcId, WalkingObject walkingObject, Chicken guide) {
        if (activeTasks.containsKey(npcId)) return;

        Location currentLocation = npcAdapter.getLocation(plugin, npcId);
        Location centerLocation = walkingObject.getWaypoint(walkingObject.getStartWaypoint()).getLocation();
        if (centerLocation == null) centerLocation = currentLocation;

        boolean isWandering = walkingObject.getWalkingMode() == WalkingMode.WANDER;

        if (!isWandering) {
            if (walkingObject.getNextWaypoint() == null)
                walkingObject.setNextWaypoint(calculateNextWaypoint(walkingObject));
            guide.getPathfinder().moveTo(walkingObject.getNextWaypoint().getLocation());
        }

        BukkitTask task = new BukkitRunnable() {
            boolean isPaused = false;
            int tickCounter = 0;
            int waitTimer = 0;

            Location lastLocation = null;

            @Override
            public void run() {
                if (this.isCancelled()) {
                    activeTasks.remove(npcId);
                    if (guide.isValid())
                        guide.remove();
                    this.cancel();
                    return;
                }
                if (!guide.isValid() || guide.isDead()) {
                    activeTasks.remove(npcId);
                    this.cancel();
                    return;
                }

                tickCounter++;
                waitTimer++;


                if (walkingObject.isStopsIfPlayer()) {

                    if (tickCounter % 20 == 0) {
                        boolean playerNearby = !guide.getLocation().getWorld().getNearbyPlayers(
                                guide.getLocation(), walkingObject.getStopsIfPlayerBlocks()).isEmpty();

                        if (playerNearby) {
                            if (!isPaused) {
                                guide.getPathfinder().stopPathfinding();
                                isPaused = true;
                            }

                            guide.teleportAsync(npcAdapter.getLocation(plugin, npcId));
                            return;
                        } else {
                            if (isPaused) {
                                isPaused = false;
                                if (!isWandering)
                                    guide.getPathfinder().moveTo(walkingObject.getNextWaypoint().getLocation());
                            }
                        }
                    }

                    if (isPaused)  {
                        return;
                    }
                }
                Location currentLoc = guide.getLocation();

                if (tickCounter % 3 == 0) {
                    boolean hasMoved = lastLocation == null ||
                            currentLoc.distanceSquared(lastLocation) > 0.001;

                    if (hasMoved) {
                        npcAdapter.updateNpcLocation(plugin, npcId, currentLoc);
                        lastLocation = currentLoc;
                    }
                }



                if (isWandering) {
                    if (guide.getPathfinder().hasPath()) return;

                    if (waitTimer % 40 != 0)
                        return;

                    Location randomPoint = walkingObject.getWanderingArea().getRandomLocation();
                    if (randomPoint != null) {
                        guide.getPathfinder().moveTo(randomPoint);
                    }

                    return;
                }

                if (currentLoc.distanceSquared(walkingObject.getNextWaypoint().getLocation()) < 1.5) {
                    walkingObject.setCurrentWaypoint(walkingObject.getNextWaypoint().getLocationId());

                    Waypoints next = calculateNextWaypoint(walkingObject);
                    walkingObject.setNextWaypoint(next);

                    boolean moveTo = guide.getPathfinder().moveTo(next.getLocation());

                    if (!moveTo) {
                        guide.teleport(walkingObject.getWaypoint(walkingObject.getStartWaypoint()).getLocation());
                        walkingObject.setCurrentWaypoint(walkingObject.getStartWaypoint());
                        walkingObject.setNextWaypoint(calculateNextWaypoint(walkingObject));
                    }
                    return;
                }

                if (!guide.getPathfinder().hasPath()) {
                    boolean retry = guide.getPathfinder().moveTo(walkingObject.getNextWaypoint().getLocation());

                    if (!retry) {
                        guide.getPathfinder().stopPathfinding();
                        guide.remove();
                        npcAdapter.updateNpcLocation(plugin, npcId, walkingObject.getWaypoint(walkingObject.getStartWaypoint()).getLocation());
                        walkingObject.setNextWaypoint(null);
                        walkingObject.setCurrentWaypoint(walkingObject.getStartWaypoint());
                        this.cancel();
                    }
                }

            }
        }.runTaskTimer(plugin, 0L, 1L);

        activeTasks.put(npcId, new ActiveNpcContext(guide, task));
    }

    private Waypoints calculateNextWaypoint(WalkingObject walkingObject) {
        Waypoints currentWaypoint = walkingObject.getWaypoint(walkingObject.getCurrentWaypoint());
        if (currentWaypoint == null) currentWaypoint = walkingObject.getWaypoint(walkingObject.getStartWaypoint());

        List<String> possibleWaypoints = currentWaypoint.getPossibleNextLocations();

        if (possibleWaypoints != null && !possibleWaypoints.isEmpty()) {
            String nextId = possibleWaypoints.get(rand.nextInt(possibleWaypoints.size()));
            return walkingObject.getWaypoint(nextId);
        } else {
            return walkingObject.getWaypoint(walkingObject.getStartWaypoint());
        }
    }

    public void cancelTask(String npcId) {
        ActiveNpcContext context = activeTasks.remove(npcId);

        if (context != null) {
            if (context.activeTask != null && !context.activeTask.isCancelled()) {
                context.activeTask.cancel();
            }

            if (context.guide != null && context.guide.isValid())
                context.guide.remove();
        }
    }

    public void cancelAll() {
        for (String id : List.copyOf(activeTasks.keySet())) {
            cancelTask(id);
        }
    }

    private record ActiveNpcContext(LivingEntity guide, BukkitTask activeTask) {
    }
}
