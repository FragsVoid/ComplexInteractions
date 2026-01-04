package org.frags.complexInteractions.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.subcommands.*; // Asumiendo que están aquí
import org.frags.complexInteractions.commands.subcommands.walkcommands.WalkingCommand;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;
import org.frags.complexInteractions.objects.conversation.Option;
import org.frags.complexInteractions.objects.walking.WalkingMode;
import org.frags.complexInteractions.objects.walking.Waypoints;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;

import org.frags.customItems.CustomItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InteractionsCommand implements CommandExecutor, TabCompleter {

    private ComplexInteractions plugin;
    private List<SubCommand> subCommands = new ArrayList<>();

    public InteractionsCommand(ComplexInteractions plugin) {
        this.plugin = plugin;
        subCommands.add(new ReloadCommand());
        subCommands.add(new ResetCooldownCommand());
        subCommands.add(new WalkingCommand());
        subCommands.add(new ConversationCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length > 0) {
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    subCommand.perform(plugin, player, args);
                    return true;
                }
            }
        }

        // Help menu
        if (!player.hasPermission("interactions.admin")) {
            return true;
        }
        player.sendMessage("--------------------------------");
        for (SubCommand subCommand : subCommands) {
            player.sendMessage(subCommand.getSyntax() + " - " + subCommand.getDescription());
        }
        player.sendMessage("--------------------------------");

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
        }

        else if (args.length >= 2) {
            String mainArg = args[0].toLowerCase();

            if (mainArg.equals("conversation") && (player.hasPermission("interactions.conversation") || player.hasPermission("interactions.admin"))) {
                return handleConversationTab(player, args);
            }

            else if (mainArg.equals("get") && player.hasPermission("interactions.get") && player.hasPermission("interactions.admin")) {
                if (args.length == 2) {
                    return StringUtil.copyPartialMatches(args[1], new ArrayList<>(CustomItems.INSTANCE.getItemProvider().getAllIds()), new ArrayList<>());
                }
            }

            else if (mainArg.equals("reset") && player.hasPermission("interactions.reset") && player.hasPermission("interactions.admin")) {
                if (args.length == 2) {
                    List<String> completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                    return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
                } else if (args.length == 3) {
                    return StringUtil.copyPartialMatches(args[2], plugin.getConversationManager().getAllNpcsId(), new ArrayList<>());
                }
            }

            else if (mainArg.equals("walk") && player.hasPermission("interactions.walk") && player.hasPermission("interactions.admin")) {
                return handleWalkTab(args);
            }
        }

        return List.of();
    }

    private List<String> handleConversationTab(Player player, String[] args) {
        if (args.length == 2) {
            List<String> actions = List.of(
                    "create", "setnpc", "setblockmovement", "startconversationradius",
                    "endconversationradius", "setstartstage", "setnoreqstage",
                    "addconversationstage", "addinterruptactions", "addrequirement",
                    "setcooldown", "setcooldownmessage", "setonlyonce",
                    "setalreadycompletedstageid", "modifystage"
            );
            return StringUtil.copyPartialMatches(args[1], actions, new ArrayList<>());
        }

        String action = args[1].toLowerCase();

        if (action.equals("create")) {
            if (args.length == 3) return List.of("<new_file_name>");
            if (args.length == 4) return getNpcIds(args[3]);
            if (args.length == 5) return List.of("<start_stage_id>");
            return List.of();
        }

        if (args.length == 3) {
            List<String> convIds = new ArrayList<>();
            for(Conversation c : plugin.getConversationManager().getAllConversations()) {
                convIds.add(c.getId());
            }
            return StringUtil.copyPartialMatches(args[2], convIds, new ArrayList<>());
        }

        String convId = args[2];
        Conversation conv = plugin.getConversationManager().getConversation(convId);
        if (conv == null) return List.of();

        if (args.length == 4) {
            switch (action) {
                case "setnpc":
                    return getNpcIds(args[3]);
                case "setblockmovement":
                case "setonlyonce":
                    return StringUtil.copyPartialMatches(args[3], List.of("true", "false"), new ArrayList<>());
                case "setstartstage":
                case "setnoreqstage":
                case "setcooldownmessage":
                case "setalreadycompletedstageid":
                case "modifystage":
                    return StringUtil.copyPartialMatches(args[3], new ArrayList<>(conv.getConversationStageMap().keySet()), new ArrayList<>());
                case "addconversationstage":
                    return List.of("<new_stage_id>");
                default:
                    return List.of();
            }
        }

        if (action.equals("modifystage")) {
            String stageId = args[3];
            ConversationStage stage = conv.getStage(stageId);
            if (stage == null) return List.of();

            if (args.length == 5) {
                List<String> stageActions = List.of(
                        "settext", "addtext", "removetext", "setdelay", "addaction",
                        "setcompletes", "addoption", "removeoption", "modifyoption"
                );
                return StringUtil.copyPartialMatches(args[4], stageActions, new ArrayList<>());
            }

            String subAction = args[4].toLowerCase();

            if (args.length == 6) {
                if (subAction.equals("removeoption") || subAction.equals("modifyoption")) {
                    List<String> optionIds = new ArrayList<>();
                    if (stage.getOptionList() != null) {
                        for (Option op : stage.getOptionList()) optionIds.add(op.getId());
                    }
                    return StringUtil.copyPartialMatches(args[5], optionIds, new ArrayList<>());
                }
                if (subAction.equals("setcompletes")) {
                    return StringUtil.copyPartialMatches(args[5], List.of("true", "false"), new ArrayList<>());
                }
                if (subAction.equals("addoption")) {
                    return List.of("<new_option_id>");
                }
            }

            if (subAction.equals("modifyoption") && args.length == 7) {
                List<String> optActions = List.of("settext", "settarget", "setnoreq", "addaction", "addreq");
                return StringUtil.copyPartialMatches(args[6], optActions, new ArrayList<>());
            }

            if (subAction.equals("modifyoption") && args.length == 8) {
                String optAction = args[6].toLowerCase();
                if (optAction.equals("settarget") || optAction.equals("setnoreq")) {
                    return StringUtil.copyPartialMatches(args[7], new ArrayList<>(conv.getConversationStageMap().keySet()), new ArrayList<>());
                }
            }
        }

        return List.of();
    }

    private List<String> handleWalkTab(String[] args) {
        if (args.length == 2) {
            List<String> walkSubCommands = List.of("npc", "speed", "mode", "start", "stops", "stopsBlocks", "location", "addlocation", "get", "setarea", "forbidden", "create");
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
                    return getNpcIds(args[3]);
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
                    break;
                case "speed":
                    return StringUtil.copyPartialMatches(args[3], List.of("0.5", "0.8", "1"), new ArrayList<>());
                case "setarea":
                    return StringUtil.copyPartialMatches(args[3], List.of("pos1", "pos2"), new ArrayList<>());
                case "forbidden":
                    return StringUtil.copyPartialMatches(args[3], List.of("add", "remove"), new ArrayList<>());
                case "create":
                    return getNpcIds(args[3]);
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
            } else if (args[1].equalsIgnoreCase("create")) {
                return List.of("speed");
            }
        }

        if (args.length == 6) {
            if (args[1].equalsIgnoreCase("forbidden")) {
                return StringUtil.copyPartialMatches(args[3], List.of("pos1", "pos2"), new ArrayList<>());
            } else if (args[1].equalsIgnoreCase("create")) {
                List<String> modes = Arrays.stream(WalkingMode.values()).map(Enum::name).toList();
                return StringUtil.copyPartialMatches(args[3], modes, new ArrayList<>());
            }
        }

        return List.of();
    }

    private List<String> getNpcIds(String arg) {
        return StringUtil.copyPartialMatches(arg, FancyNpcsPlugin.get().getNpcManager().getAllNpcs().stream()
                .map(npc -> npc.getData().getName()).toList(), new ArrayList<>());
    }
}