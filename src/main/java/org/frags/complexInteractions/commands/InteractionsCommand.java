package org.frags.complexInteractions.commands;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.subcommands.*;
import org.frags.complexInteractions.commands.subcommands.walkcommands.WalkingCommand;
import org.frags.complexInteractions.objects.walking.WalkingMode;
import org.frags.complexInteractions.objects.walking.Waypoints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InteractionsCommand implements CommandExecutor, TabCompleter {

    private ComplexInteractions plugin;

    private List<SubCommand> subCommands = new ArrayList<>();

    public InteractionsCommand(ComplexInteractions plugin) {
        this.plugin = plugin;
        subCommands.add(new ReloadCommand());
        subCommands.add(new ResetCooldownCommand());
        subCommands.add(new ImportCommand());
        subCommands.add(new GetItemCommand());
        subCommands.add(new WalkingCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player))
            return false;
        if (args.length > 0) {
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    subCommand.perform(plugin, player, args);
                }
            }
        } else {
            if (!player.hasPermission("interactions.admin")) {
                return true;
            }
            player.sendMessage("--------------------------------");
            for (SubCommand subCommand : subCommands) {
                player.sendMessage(subCommand.getSyntax() + " - " + subCommand.getDescription());
            }
            player.sendMessage("--------------------------------");

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                if (subCommand.hasPermission(player))
                    completions.add(subCommand.getName());
            }

            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length >= 2) {

            if (args[0].equalsIgnoreCase("get") && player.hasPermission("interactions.get") && player.hasPermission("interactions.admin")) {
                if (args.length == 2) {
                    return StringUtil.copyPartialMatches(args[1], new ArrayList<>(plugin.getItemManager().getAllIds()), new ArrayList<>());
                }
            }
            else if (args[0].equalsIgnoreCase("reset") && player.hasPermission("interactions.reset") && player.hasPermission("interactions.admin")) {
                if (args.length == 2) {
                    List<String> completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                    return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
                } else if (args.length == 3) {
                    return StringUtil.copyPartialMatches(args[2], plugin.getConversationManager().getAllNpcsId(), new ArrayList<>());
                }
            } else if (args[0].equalsIgnoreCase("walk") && player.hasPermission("interactions.walk") && player.hasPermission("interactions.admin")) {
                if (args.length == 2) {
                    List<String> walkSubCommands = List.of("npc", "speed", "mode", "start", "stops", "stopsBlocks", "location", "addlocation", "get", "setarea", "forbidden");
                    return StringUtil.copyPartialMatches(args[1], walkSubCommands, new ArrayList<>());
                }

                if (args.length == 3) {
                    return StringUtil.copyPartialMatches(args[2], plugin.getWalkingManager().getAllIds(), new ArrayList<>());
                }

                if (args.length == 4) {
                    String subAction = args[1].toLowerCase();
                    String fileName = args[2];

                    switch (subAction) {
                        case "npc":
                            return StringUtil.copyPartialMatches(args[3], FancyNpcsPlugin.get().getNpcManager().getAllNpcs().stream().map((Npc npc) -> {return npc.getData().getName();}).toList(),  new ArrayList<>());
                        case "mode":
                            List<String> modes = Arrays.stream(WalkingMode.values()).map(Enum::name).toList();
                            return StringUtil.copyPartialMatches(args[3], modes, new ArrayList<>());
                        case "stops":
                            return StringUtil.copyPartialMatches(args[3], List.of("true", "false"), new ArrayList<>());
                        case "start":
                        case "addlocation":
                            if (plugin.getWalkingManager().exists(fileName)) {
                                List<String> waypoints = plugin.getWalkingManager().getWalking(fileName).getWaypoints().values()
                                        .stream().map(Waypoints::getLocationId).toList();
                                return StringUtil.copyPartialMatches(args[3], waypoints, new ArrayList<>());
                            }
                        case "speed":
                            return StringUtil.copyPartialMatches(args[3], List.of("0.5", "0.8", "1"), new ArrayList<>());
                        case "setarea":
                            return StringUtil.copyPartialMatches(args[3], List.of("pos1", "pos2"), new ArrayList<>());
                        case "forbidden":
                            return StringUtil.copyPartialMatches(args[3], List.of("add", "remove"), new ArrayList<>());

                    }
                }

                if (args.length == 5) {
                    if (args[1].equalsIgnoreCase("addlocation")) {
                        String fileName = args[2];
                        if (plugin.getWalkingManager().exists(fileName)) {
                            List<String> waypoints = plugin.getWalkingManager().getWalking(fileName).getWaypoints().values()
                                    .stream().map(Waypoints::getLocationId).toList();
                            return StringUtil.copyPartialMatches(args[4], waypoints, new ArrayList<>());
                        }
                    }
                }

                if (args.length == 6) {
                    if (args[1].equalsIgnoreCase("forbidden")) {
                        return StringUtil.copyPartialMatches(args[3], List.of("pos1", "pos2"), new ArrayList<>());
                    }
                }
            }

        }

        return List.of();
    }
}
