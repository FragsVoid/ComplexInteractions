package org.frags.complexInteractions.managers;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BoundingBox;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.npcmovermanager.NpcAIMover;
import org.frags.complexInteractions.objects.walking.WalkingMode;
import org.frags.complexInteractions.objects.walking.WalkingObject;
import org.frags.complexInteractions.objects.walking.WanderingArea;
import org.frags.complexInteractions.objects.walking.Waypoints;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WalkingManager {

    private final ComplexInteractions plugin;
    private Map<String, WalkingObject> walkingObjectMap = new HashMap<>();

    private NpcAIMover npcAIMover;

    public WalkingManager(ComplexInteractions plugin) {
        this.plugin = plugin;
        this.npcAIMover = new NpcAIMover(plugin);
        load(false);
    }

    public boolean exists(String file) {
        return walkingObjectMap.containsKey(file);
    }

    public WalkingObject getWalkingByNpcId(String file) {
        if (file == null) return null;

        for (WalkingObject obj : walkingObjectMap.values()) {
            if (obj.getNpcId().equalsIgnoreCase(file)) {
                return obj;
            }
        }
        return null;
    }

    public WalkingObject getWalking(String file) {
        return walkingObjectMap.get(file);
    }

    public Set<String> getAllIds() {
        return walkingObjectMap.keySet();
    }

    public void load(boolean reloads) {
        walkingObjectMap.clear();

        File folder = new File(plugin.getDataFolder(), "movePaths");
        if (!folder.exists()) {
            try {
                plugin.saveResource("movePaths/example.yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Didn't find example.yml");
            }
        }

        String[] files = folder.list();
        if (files == null)
            return;

        for (String fileName : files) {
            String walkingPathId = fileName.replace(".yml", "");

            File targetFile = new File(folder, fileName);
            FileConfiguration config = YamlConfiguration.loadConfiguration(targetFile);

            String npcId = config.getString("npcId");
            float speed = (float) config.getDouble("speed");
            String walkingMode = config.getString("mode");
            if (walkingMode == null) {
                plugin.getLogger().severe("Walking mode not found");
                continue;
            }
            WalkingMode mode = WalkingMode.getMode(walkingMode);
            if (mode == null) {
                plugin.getLogger().severe("Walking mode not found for " + fileName);
                continue;
            }

            String startWaypoint = config.getString("start_waypoint");

            boolean stopsIfPlayer = config.getBoolean("stops_if_player");
            int stopsIfPlayerBlocks = config.getInt("stops_player_blocks");

            Map<String, Waypoints> waypoints = new HashMap<>();
            ConfigurationSection waypointsSection = config.getConfigurationSection("waypoints");
            if (waypointsSection != null) {
                for (String key : waypointsSection.getKeys(false)) {
                    ConfigurationSection waypointSection = waypointsSection.getConfigurationSection(key);
                    Location location = waypointSection.getLocation("location");
                    List<String> possibleLocations = waypointSection.getStringList("possible_locations");
                    waypoints.put(key, new Waypoints(this, walkingPathId, key, location, possibleLocations));
                }
            }

            WanderingArea wanderingArea = null;

            if (mode == WalkingMode.WANDER) {
                wanderingArea = new WanderingArea(config.getLocation("first_point"), config.getLocation("second_point"));
            }
            if (wanderingArea != null) {
                ConfigurationSection section = config.getConfigurationSection("forbidden");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        ConfigurationSection forbiddenSection = section.getConfigurationSection(key);
                        Location firstLocation = forbiddenSection.getLocation("firstLocation");
                        Location secondLocation = forbiddenSection.getLocation("secondLocation");
                        wanderingArea.addForbiddenZone(firstLocation, secondLocation);
                    }
                }

            }


            walkingObjectMap.put(walkingPathId, new WalkingObject(npcId, speed, mode, null, startWaypoint, waypoints,
                    stopsIfPlayer, stopsIfPlayerBlocks, wanderingArea));
        }

        if (reloads) {
            for (Npc npc : FancyNpcsPlugin.get().getNpcManager().getAllNpcs()) {
                String id = npc.getData().getName();

                if (getWalkingByNpcId(id) == null) continue;
                getNpcAIMover().startWalkingTask(id);
            }
        }

    }

    public void save() {
        File folder = new File(plugin.getDataFolder(), "movePaths");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (Map.Entry<String, WalkingObject> entry : walkingObjectMap.entrySet()) {
            String walkingPathId = entry.getKey();
            WalkingObject obj = entry.getValue();

            File file = new File(folder, walkingPathId + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            config.set("npcId", obj.getNpcId());
            config.set("speed", obj.getSpeed());

            if (obj.getWalkingMode() != null) {
                config.set("mode", obj.getWalkingMode().name());
            }

            config.set("start_waypoint", obj.getStartWaypoint());
            config.set("stops_if_player", obj.isStopsIfPlayer());
            config.set("stops_player_blocks", obj.getStopsIfPlayerBlocks());

            config.set("waypoints", null);

            config.set("first_point", obj.getWanderingArea().getMinLocation());
            config.set("second_point", obj.getWanderingArea().getMaxLocation());

            ConfigurationSection configSection = config.getConfigurationSection("forbidden");
            if (configSection == null) {
                configSection = config.createSection("forbidden");
            }
            World world = obj.getWanderingArea().getMaxLocation().getWorld();
            int counter = 0;
            for (BoundingBox box : obj.getWanderingArea().getForbiddenZones()) {
                counter++;
                Location firstLocation = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
                Location secondLocation = new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ());
                configSection.set(counter + ".firstLocation", firstLocation);
                configSection.set(counter + ".secondLocation", secondLocation);
            }

            if (obj.getWaypoints() != null) {
                for (Map.Entry<String, Waypoints> wpEntry : obj.getWaypoints().entrySet()) {
                    String wpKey = wpEntry.getKey();
                    Waypoints wp = wpEntry.getValue();

                    String path = "waypoints." + wpKey;

                    config.set(path + ".location", wp.getLocation());

                    config.set(path + ".possible_locations", wp.getPossibleNextLocations());
                }
            }

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save file: " + file.getName());
                e.printStackTrace();
            }
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".yml")) continue;

                String idFromFile = file.getName().replace(".yml", "");

                if (!walkingObjectMap.containsKey(idFromFile)) {
                    if (file.delete()) {
                        plugin.getLogger().info("Removed file: " + file.getName());
                    }
                }
            }
        }

        plugin.getLogger().info("Every route have been saved.");
    }


    public NpcAIMover getNpcAIMover() {
        return npcAIMover;
    }
}
