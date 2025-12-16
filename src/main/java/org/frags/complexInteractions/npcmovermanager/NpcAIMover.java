package org.frags.complexInteractions.npcmovermanager;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.npcmovermanager.goals.AvoidForbiddenGoal;
import org.frags.complexInteractions.objects.walking.WalkingMode;
import org.frags.complexInteractions.objects.walking.WalkingObject;
import org.frags.complexInteractions.objects.walking.Waypoints;

import java.util.*;

public class NpcAIMover {

    private ComplexInteractions plugin;

    private Map<String, ActiveNpcContext> activeTasks = new HashMap<>();

    private final Random rand = new Random();

    public NpcAIMover(ComplexInteractions plugin) {
        this.plugin = plugin;
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

        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        if (npc == null || activeTasks.containsKey(npcId)) return;

        Location start = npc.getData().getLocation().clone().add(0, 0.5, 0);

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
                    startMoving(npc, walkingObject, guide);
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

    private void startMoving(Npc npc, WalkingObject walkingObject, Chicken guide) {
        if (activeTasks.containsKey(npc.getData().getName())) return;

        Location centerLocation = walkingObject.getWaypoint(walkingObject.getStartWaypoint()).getLocation();
        if (centerLocation == null) centerLocation = npc.getData().getLocation();

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
                    activeTasks.remove(npc.getData().getId());
                    if (guide.isValid())
                        guide.remove();
                    this.cancel();
                    return;
                }
                if (!guide.isValid() || guide.isDead()) {
                    activeTasks.remove(npc.getData().getId());
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

                            guide.teleportAsync(npc.getData().getLocation());
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
                        npc.getData().setLocation(currentLoc);
                        double viewRange = npc.getData().getVisibilityDistance();

                        for (Player p : currentLoc.getWorld().getNearbyPlayers(currentLoc, viewRange)) {
                            npc.move(p, false);
                        }
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
                        npc.getData().setLocation(walkingObject.getWaypoint(walkingObject.getStartWaypoint()).getLocation());
                        walkingObject.setNextWaypoint(null);
                        walkingObject.setCurrentWaypoint(walkingObject.getStartWaypoint());
                        this.cancel();
                    }
                }

            }
        }.runTaskTimer(plugin, 0L, 1L);

        activeTasks.put(npc.getData().getName(), new ActiveNpcContext(guide, task));
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

    private static class ActiveNpcContext {
        final BukkitTask activeTask;
        final Chicken guide;

        public ActiveNpcContext(Chicken guide, BukkitTask activeTask) {
            this.guide = guide;
            this.activeTask = activeTask;
        }
    }
}
