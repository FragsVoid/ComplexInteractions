package org.frags.complexInteractions.commands.subcommands.walkcommands;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.SubCommand;
import org.frags.complexInteractions.objects.walking.WalkingMode;
import org.frags.complexInteractions.objects.walking.WalkingObject;
import org.frags.complexInteractions.objects.walking.Waypoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalkingCommand extends SubCommand {
    @Override
    public String getName() {
        return "walk";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "/interactions walk";
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission("interactions.walk") && player.hasPermission("interactions.admin");
    }

    @Override
    public void perform(ComplexInteractions plugin, Player player, String[] args) {
        if (!hasPermission(player)) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return;
        }

        String arg = args[1];

        if (arg.equalsIgnoreCase("npc")) {
            setNpc(plugin, player, args);
        } else if (arg.equalsIgnoreCase("speed")) {
            setSpeed(plugin, player, args);
        } else if (arg.equalsIgnoreCase("mode")) {
            setMode(plugin, player, args);
        } else if (arg.equalsIgnoreCase("start")) {
            setStartWaypoint(plugin, player, args);
        } else if (arg.equalsIgnoreCase("stops")) {
            setStops(plugin, player, args);
        } else if (arg.equalsIgnoreCase("stopsBlocks")) {
            setStopsBlocks(plugin, player, args);
        } else if (arg.equalsIgnoreCase("location")) {
            setLocation(plugin, player, args);
        } else if (arg.equalsIgnoreCase("addlocation")) {
            setAddLocation(plugin, player, args);
        } else if (arg.equalsIgnoreCase("get")) {
            if (args.length != 3) {
                player.sendMessage("Wrong usage: " + getSyntax() + " get <file>");
                return;
            }

            String file = args[2];
            if (!plugin.getWalkingManager().exists(file)) {
                player.sendMessage("File not found: " + file);
                return;
            }
            WalkingObject walkingObject = plugin.getWalkingManager().getWalking(file);
            player.sendMessage("Id " + walkingObject.getNpcId());
            player.sendMessage("Start " + walkingObject.getStartWaypoint());
            List<Waypoints> waypoints = walkingObject.getAllWaypoints();
            player.sendMessage("Locations: " + waypoints.toString());
            player.sendMessage("List of waypoints per: ");
            for (Waypoints waypoint : waypoints) {
                player.sendMessage(waypoint.getLocationId() + " " + waypoint.getPossibleNextLocations().toString());
            }
        } else if (arg.equalsIgnoreCase("setarea")) {
            setArea(plugin, player, args);
        } else if (arg.equalsIgnoreCase("forbidden")) {
            setForbidden(plugin, player, args);
        } else if (arg.equalsIgnoreCase("create")) {
            createWalking(plugin, player, args);
        } else if (arg.equalsIgnoreCase("clone")) {
            if (args.length < 5) {
                player.sendMessage("Usage: /interactions walk clone <file> <newfile> <npcid>");
                return;
            }

            String file = args[2];
            if (!plugin.getWalkingManager().exists(file)) {
                player.sendMessage("File not found: " + file);
                return;
            }

            String newfile = args[3];
            if (plugin.getWalkingManager().exists(newfile)) {
                player.sendMessage("File already exists: " + newfile);
                return;
            }

            String npcId = args[4];

            WalkingObject walkingObject = plugin.getWalkingManager().getWalking(file);

            Map<String, Waypoints> newWaypoints = new HashMap<>();
            if (walkingObject.getWaypoints() != null) {
                newWaypoints.putAll(walkingObject.getWaypoints());
            }

            WalkingObject newWalkingObject = new WalkingObject(npcId, walkingObject.getSpeed(), walkingObject.getWalkingMode(),
                    walkingObject.getRegion(), walkingObject.getStartWaypoint(), newWaypoints, walkingObject.isStopsIfPlayer(),
                    walkingObject.getStopsIfPlayerBlocks(), walkingObject.getWanderingArea());

            plugin.getWalkingManager().addWalkingObject(newfile, newWalkingObject);
            player.sendMessage("Done");
        }
    }

    public void createWalking(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage("Usage: /interactions walk create <file> <npcid> <speed> <walkingmode>");
            return;
        }

        String file = args[2];

        String npcId = args[3];
        String speedStr = args[4];
        float speed = 0;
        try {
            speed = Float.parseFloat(speedStr);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid speed: " + speedStr);
            return;
        }

        WalkingMode walkingMode = WalkingMode.getMode(args[5].toUpperCase());
        if (walkingMode == null) {
            player.sendMessage("Invalid walking mode: " + walkingMode);
            return;
        }
        plugin.getWalkingManager().addWalkingObject(file, new WalkingObject(npcId, speed, walkingMode, null,
                null, new HashMap<>(), false, 0, null));

        player.sendMessage("Successfully created walking object.");
    }

    public void setForbidden(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage("Usage: /interactions walk forbidden <file> <add|remove> <zoneId> [pos1|pos2]");
            return;
        }

        String file = args[2];
        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found.");
            return;
        }

        String action = args[3];
        String zoneId = args[4];
        WalkingObject walkingObject = plugin.getWalkingManager().getWalking(file);

        if (action.equalsIgnoreCase("add")) {
            if (args.length != 6) {
                player.sendMessage("Usage: ... add <zoneId> <pos1|pos2>");
                return;
            }

            String point = args[5];

            if (point.equalsIgnoreCase("pos1")) {
                walkingObject.setForbiddenPos1(zoneId, player.getLocation());
                player.sendMessage("Forbidden Zone '" + zoneId + "' Pos1 set!");
            } else if (point.equalsIgnoreCase("pos2")) {
                boolean success = walkingObject.createForbiddenZone(zoneId, player.getLocation());
                if (success) {
                    player.sendMessage("Forbidden Zone '" + zoneId + "' created successfully!");
                } else {
                    player.sendMessage("Error: Set Pos1 first using /... add " + zoneId + " pos1");
                }
            }

        } else if (action.equalsIgnoreCase("remove")) {
            walkingObject.removeForbiddenZone(zoneId);
            player.sendMessage("Forbidden Zone '" + zoneId + "' removed.");
        }
    }

    public void setArea(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " setarea <file> <pos1|pos2>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }

        String pointType = args[3].toLowerCase();
        WalkingObject walkingObject = plugin.getWalkingManager().getWalking(file);

        if (pointType.equals("pos1")) {
            walkingObject.setAreaPos1(player.getLocation());
            player.sendMessage("Set Area Position 1 at your location.");
        } else if (pointType.equals("pos2")) {
            walkingObject.setAreaPos2(player.getLocation());
            player.sendMessage("Set Area Position 2 at your location.");
        } else {
            player.sendMessage("Invalid position argument. Use 'pos1' or 'pos2'.");
            return;
        }

        if (walkingObject.getAreaPos1() != null && walkingObject.getAreaPos2() != null) {
            walkingObject.updateWanderingArea();
            player.sendMessage("Wandering Area successfully updated!");
        }
    }

    public void setNpc(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " npc <file> <npc>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }

        String npc = args[3];
        plugin.getWalkingManager().getWalking(file).setNpc(npc);
        player.sendMessage("Done!");
    }

    public void setSpeed(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " speed <file> <speed>");
            return;
        }
        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }

        String speedStr = args[3];
        float speed = 0;
        try {
            speed = Float.parseFloat(speedStr);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid speed: " + speedStr);
            return;
        }

        plugin.getWalkingManager().getWalking(file).setSpeed(speed);
        player.sendMessage("Done!");
    }

    public void setMode(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " mode <file> <mode>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }

        String mode = args[3];
        WalkingMode walkingMode = WalkingMode.getMode(mode);
        if (walkingMode == null) {
            player.sendMessage("Invalid mode: " + mode);
            return;
        }

        plugin.getWalkingManager().getWalking(file).setWalkingMode(walkingMode);
        player.sendMessage("Done!");
    }

    public void setStartWaypoint(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " start <file> <startWaypoint>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }
        String startWaypoint = args[3];

        plugin.getWalkingManager().getWalking(file).setStartWaypoint(startWaypoint);
        player.sendMessage("Done!");
    }

    public void setStops(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " stops <file> <boolean>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }

        String stopsStr = args[3];
        boolean stops = Boolean.parseBoolean(stopsStr);

        plugin.getWalkingManager().getWalking(file).setStopsIfPlayer(stops);
        player.sendMessage("Done!");
    }

    public void setStopsBlocks(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " stopsBlocks <file> <blocks>");
            return;
        }
        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
            return;
        }

        String blocksStr = args[3];
        int blocks = 0;
        try {
            blocks = Integer.parseInt(blocksStr);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid speed: " + blocksStr);
            return;
        }

        plugin.getWalkingManager().getWalking(file).setStopsIfPlayerBlocks(blocks);
        player.sendMessage("Done!");
    }

    public void setLocation(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage("Wrong usage: " + getSyntax() + " location <file> <id>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
        }

        String id = args[3];

        WalkingObject walkingObject = plugin.getWalkingManager().getWalking(file);
        List<Waypoints> waypoints = walkingObject.getWaypoints().values().stream().toList();

        for (Waypoints waypoint : waypoints) {
            if (waypoint.getLocationId().equalsIgnoreCase(id)) {
                player.sendMessage("Location already exists: " + id);
                return;
            }
        }

        Waypoints waypoint = new Waypoints(plugin.getWalkingManager(), file, id, player.getLocation(), null);
        walkingObject.addWaypoint(waypoint);
        player.sendMessage("Done!");
    }

    public void setAddLocation(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length != 5) {
            player.sendMessage("Wrong usage: " + getSyntax() + " addLocation <file> <id> <possibleId>");
            return;
        }

        String file = args[2];

        if (!plugin.getWalkingManager().exists(file)) {
            player.sendMessage("File not found: " + file);
        }

        String id = args[3];
        String possibleId = args[4];

        WalkingObject walkingObject = plugin.getWalkingManager().getWalking(file);
        List<Waypoints> waypoints = walkingObject.getWaypoints().values().stream().toList();
        for (Waypoints waypoint : waypoints) {
            if (waypoint.getLocationId().equalsIgnoreCase(id)) {
                waypoint.addPossibleNextLocations(possibleId);
                player.sendMessage("Added possible location: " + possibleId);
                return;
            }
        }

        player.sendMessage("Didnt find that location!");
    }
}
