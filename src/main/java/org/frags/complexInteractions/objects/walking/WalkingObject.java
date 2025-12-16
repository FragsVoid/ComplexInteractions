package org.frags.complexInteractions.objects.walking;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WalkingObject {

    private String npcId;
    private float speed;
    private WalkingMode walkingMode;

    private String currentWaypoint;
    private Waypoints nextWaypoint = null;

    private Location areaPos1;
    private Location areaPos2;

    private String region;
    private String startWaypoint;

    private Map<String, Waypoints> waypoints;

    private final Map<String, Location> tempForbiddenPos1 = new HashMap<>();

    private Map<String, BoundingBox> forbiddenZonesMap = new HashMap<>();

    private WanderingArea wanderingArea;

    private boolean stopsIfPlayer;
    private int stopsIfPlayerBlocks;

    public WalkingObject(String npcId, float speed, WalkingMode walkingMode, String region, String startWaypoint, Map<String, Waypoints> waypoints, boolean stopsIfPlayer, int stopsIfPlayerBlocks, WanderingArea wanderingArea) {
        this.npcId = npcId;
        this.speed = speed;
        this.walkingMode = walkingMode;
        this.region = region;
        this.startWaypoint = startWaypoint;
        this.currentWaypoint = startWaypoint;
        this.waypoints = waypoints;
        this.stopsIfPlayer = stopsIfPlayer;
        this.stopsIfPlayerBlocks = stopsIfPlayerBlocks;
        this.wanderingArea = wanderingArea;
    }

    public void setNpc(String npcId) {
        this.npcId = npcId;
    }

    public List<Waypoints> getAllWaypoints() {
        return waypoints.values().stream().toList();
    }

    public void setForbiddenPos1(String zoneId, Location loc) {
        tempForbiddenPos1.put(zoneId, loc);
    }

    public boolean createForbiddenZone(String zoneId, Location pos2) {
        if (!tempForbiddenPos1.containsKey(zoneId)) return false;

        Location pos1 = tempForbiddenPos1.get(zoneId);
        if (!pos1.getWorld().equals(pos2.getWorld())) return false;

        BoundingBox box = BoundingBox.of(pos1, pos2);
        forbiddenZonesMap.put(zoneId, box);

        if (wanderingArea != null) {
            wanderingArea.addForbiddenZone(box);
        }

        tempForbiddenPos1.remove(zoneId);
        return true;
    }

    public void removeForbiddenZone(String zoneId) {
        forbiddenZonesMap.remove(zoneId);
        updateWanderingArea();
    }

    public void updateWanderingArea() {
        if (areaPos1 != null && areaPos2 != null) {
            this.wanderingArea = new WanderingArea(areaPos1, areaPos2);
            for (BoundingBox box : forbiddenZonesMap.values()) {
                this.wanderingArea.addForbiddenZone(box);
            }
        }
    }

    public Collection<BoundingBox> getForbiddenZonesMap() {
        return forbiddenZonesMap.values();
    }

    public Waypoints getWaypoint(String waypointId) {
        return waypoints.get(waypointId);
    }

    public String getCurrentWaypoint() {
        return currentWaypoint;
    }

    public void setCurrentWaypoint(String currentWaypoint) {
        this.currentWaypoint = currentWaypoint;
    }

    public Map<String, Waypoints> getWaypoints() {
        return waypoints;
    }

    public String getNpcId() {
        return npcId;
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }

    public Location getAreaPos1() {
        return areaPos1;
    }

    public void setAreaPos1(Location areaPos1) {
        this.areaPos1 = areaPos1;
    }

    public Location getAreaPos2() {
        return areaPos2;
    }

    public void setAreaPos2(Location areaPos2) {
        this.areaPos2 = areaPos2;
    }

    public WanderingArea getWanderingArea() {
        if (wanderingArea == null && areaPos1 != null && areaPos2 != null) {
            updateWanderingArea();
        }
        return wanderingArea;
    }

    public float getSpeed() {
        return speed;
    }

    public WalkingMode getWalkingMode() {
        return walkingMode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStartWaypoint() {
        return startWaypoint;
    }


    public boolean isStopsIfPlayer() {
        return stopsIfPlayer;
    }

    public int getStopsIfPlayerBlocks() {
        return stopsIfPlayerBlocks;
    }

    public void addWaypoint(Waypoints waypoint) {
        waypoints.put(waypoint.getLocationId(), waypoint);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setWalkingMode(WalkingMode walkingMode) {
        this.walkingMode = walkingMode;
    }

    public void setStartWaypoint(String startWaypoint) {
        this.startWaypoint = startWaypoint;
    }

    public void setStopsIfPlayer(boolean stopsIfPlayer) {
        this.stopsIfPlayer = stopsIfPlayer;
    }

    public void setStopsIfPlayerBlocks(int stopsIfPlayerBlocks) {
        this.stopsIfPlayerBlocks = stopsIfPlayerBlocks;
    }

    public Waypoints getNextWaypoint() {
        return nextWaypoint;
    }

    public void setNextWaypoint(Waypoints nextWaypoint) {
        this.nextWaypoint = nextWaypoint;
    }
}
