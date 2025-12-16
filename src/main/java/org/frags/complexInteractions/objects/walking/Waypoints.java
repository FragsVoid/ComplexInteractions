package org.frags.complexInteractions.objects.walking;

import org.bukkit.Location;
import org.frags.complexInteractions.managers.WalkingManager;

import java.util.ArrayList;
import java.util.List;

public class Waypoints {

    private final WalkingManager walkingManager;
    private String pathId;

    private final String locationId;

    private Location location;
    private List<String> possibleNextLocations;

    public Waypoints(WalkingManager walkingManager, String pathId, String locationId, Location location, List<String> possibleNextLocations) {
        this.walkingManager = walkingManager;
        this.pathId = pathId;
        this.locationId = locationId;
        this.location = location;
        if (possibleNextLocations == null) {
            possibleNextLocations = new ArrayList<>();
        }
        this.possibleNextLocations = possibleNextLocations;
    }

    public Location getLocation() {
        return location;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getPossibleNextLocations() {
        return possibleNextLocations;
    }

    public boolean addPossibleNextLocations(String id) {
        WalkingObject walkingObject = walkingManager.getWalking(pathId);
        List<Waypoints> waypoints = walkingObject.getWaypoints().values().stream().toList();
        if (id.equalsIgnoreCase(locationId) || !waypoints.contains(this)) {
            return false;
        }

        possibleNextLocations.add(id);
        return true;
    }
}
