package org.frags.complexInteractions.commands.subcommands;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.SubCommand;
import org.frags.complexInteractions.objects.conversation.*;
import org.frags.complexInteractions.objects.conversation.factories.ActionFactory;
import org.frags.complexInteractions.objects.conversation.factories.RequirementFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class ConversationCommand extends SubCommand {
    @Override
    public String getName() {
        return "conversation";
    }

    @Override
    public String getDescription() {
        return "conversation command";
    }

    @Override
    public String getSyntax() {
        return "/interactions conversation <action> <id> [args...]";
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission("interactions.conversation") && player.hasPermission("interactions.admin");
    }

    @Override
    public void perform(ComplexInteractions plugin, Player player, String[] args) {
        if (!hasPermission(player)) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage("Use: " + getSyntax());
            return;
        }

        String arg = args[1].toLowerCase();

        if (arg.equals("create")) {
            createConversation(plugin, player, args);
            return;
        }

        if (args.length < 3) {
            player.sendMessage("Specify npcId!");
            return;
        }

        String npcId = args[2];
        Conversation conversation = plugin.getConversationManager().getConversation(npcId);

        if (conversation == null) {
            player.sendMessage("This id does not have a conversation");
            return;
        }

        switch (arg) {
            case "setnpc":
                if (args.length < 4) {
                    player.sendMessage("Use: ... setnpc <convId> <npcId>");
                    return;
                }

                conversation.setNpcId(args[3]);
                player.sendMessage("Npc ID updated to " + args[3]);
                break;
            case "setblockmovement":
                if (args.length < 4) {
                    player.sendMessage("Use: ... setblockmovement <convId> <true/false>");
                    return;
                }
                boolean block = Boolean.parseBoolean(args[3]);
                conversation.setBlockMovement(block);
                player.sendMessage("Block Movement updated to: " + block);
                break;
            case "startconversationradius":
                if (args.length < 4) return;
                try {
                    int radius = Integer.parseInt(args[3]);
                    conversation.setStarConversationRadius(radius);
                    player.sendMessage("Start conversation radius updated to: " + radius);
                } catch (NumberFormatException e) {
                    player.sendMessage("It must be a number.");
                }
                break;

            case "endconversationradius":
                if (args.length < 4) return;
                try {
                    int radius = Integer.parseInt(args[3]);
                    conversation.setEndConversationRadius(radius);
                    player.sendMessage("EndRadius updated to: " + radius);
                } catch (NumberFormatException e) {
                    player.sendMessage("It must be a number.");
                }
                break;

            case "setstartstage":
                if (args.length < 4) return;
                conversation.setStartStageId(args[3]);
                player.sendMessage("Start Stage updated to: " + args[3]);
                break;
            case "setnoreqstage":
                if (args.length < 4) return;
                conversation.setNoReqStageId(args[3]);
                player.sendMessage("No Requirement Stage updated to: " + args[3]);
                break;

            case "addconversationstage":
                if (args.length < 4) {
                    player.sendMessage("Use: ... addconversationstage <convId> <stageId>");
                    return;
                }
                String newStageId = args[3];
                if (conversation.getConversationStageMap().containsKey(newStageId)) {
                    player.sendMessage("This stage already exists.");
                    return;
                }
                ConversationStage newStage = new ConversationStage(
                        conversation.getId(), newStageId, new ArrayList<>(), 0,
                        new ArrayList<>(), new ArrayList<>(), null
                );
                conversation.addConversationStage(newStageId, newStage);
                player.sendMessage("Stage '" + newStageId + "' added.");
                break;
            case "addinterruptactions":
                if (args.length < 4) {
                    player.sendMessage("§cUso: ... addinterruptactions <convId> <action string>");
                    return;
                }
                String actionStr = getFullString(args, 3);
                Action parsedAction = ActionFactory.parse(actionStr);

                if (parsedAction != null) {
                    conversation.getInterruptActions().add(parsedAction);
                    player.sendMessage("Interrupt Action added: " + actionStr);
                } else {
                    player.sendMessage("Invalid action.");
                }
                break;

            case "addrequirement":
                if (args.length < 4) {
                    player.sendMessage("§cUso: ... addrequirement <convId> <req string>");
                    return;
                }
                String reqStr = getFullString(args, 3);
                Requirement req = RequirementFactory.parse(reqStr, plugin.getItemManager(), "Requirement failed");

                if (req != null) {
                    conversation.getRequirements().add(req);
                    player.sendMessage("Added requirement.");
                } else {
                    player.sendMessage("Invalid requirement.");
                }
                break;

            case "setcooldown":
                if (args.length < 4) return;
                try {
                    long cooldown = Long.parseLong(args[3]);
                    conversation.setCooldown(cooldown);
                    player.sendMessage("Cooldown updated to: " + cooldown);
                } catch (NumberFormatException e) {
                    player.sendMessage("Cooldown must be a number.");
                }
                break;

            case "setcooldownmessage":
                if (args.length < 4) return;
                conversation.setCooldownMessage(args[3]);
                player.sendMessage("Cooldown stage id updated to: " + args[3]);
                break;

            case "setonlyonce":
                if (args.length < 4) return;
                boolean once = Boolean.parseBoolean(args[3]);
                conversation.setOnlyOnce(once);
                player.sendMessage("Only Once updated to: " + once);
                break;

            case "setalreadycompletedstageid":
                if (args.length < 4) return;
                conversation.setAlreadyCompletedStageId(args[3]);
                player.sendMessage("Already Completed Stage updated to: " + args[3]);
                break;
            case "modifystage":
                modifyStage(player, conversation, args);
                break;
            default:
                player.sendMessage("Unknown command.");
                break;
        }
    }

    private String getFullString(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        return sb.toString().trim();
    }

    private void  createConversation(ComplexInteractions plugin, Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage("Usage: /interactions conversation create <file> <npcid> <startStage>");
            return;
        }

        String file = args[2];
        String npcId = args[3];
        String startStage = args[4];

        if (plugin.getConversationManager().fileExists(file)) {
            player.sendMessage("This conversation already exists.");
            return;
        }



        plugin.getConversationManager().addConversation(npcId, new Conversation(file, npcId, false, false, 0, 0,
                startStage, null, null, new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                0, null, false, null));
        player.sendMessage("Conversation Created");
    }

    private void modifyStage(Player player, Conversation conversation, String[] args) {
        if (args.length < 5) {
            player.sendMessage("Use: ... modifyStage <convId> <stageId> <settext/delay/addaction/completes> <value>");
            return;
        }

        String stageId = args[3];
        ConversationStage stage = conversation.getStage(stageId);

        if (stage == null) {
            player.sendMessage("§cThe stage '" + stageId + "' does not exist.");
            return;
        }

        String subAction = args[4].toLowerCase();

        switch (subAction) {
            case "settext":
                String text = getFullString(args, 5);
                stage.getText().clear();
                stage.getText().add(text);
                player.sendMessage("Setted text");
                break;

            case "addtext":
                String line = getFullString(args, 5);
                stage.getText().add(line);
                player.sendMessage("Added text");
                break;

            case "removetext":
                String indexStr = args[5];
                int index = 0;
                try {
                    index = Integer.parseInt(indexStr);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid number");
                    return;
                }
                stage.getText().remove(index);
                player.sendMessage("Removed text");
                break;

            case "setdelay":
                try {
                    long delay = Long.parseLong(args[5]);
                    stage.setDelay(delay);
                    player.sendMessage("Updated delay");
                } catch (Exception e) {
                    player.sendMessage("Invalid number");
                }
                break;

            case "addaction":
                String actionStr = getFullString(args, 5);
                Action act = ActionFactory.parse(actionStr);
                if (act != null) {
                    stage.getActions().add(act);
                    player.sendMessage("Added action");
                }
                break;

            case "setcompletes":
                String comp = args[5];
                stage.setCompletesConversation(comp);
                player.sendMessage("§aCompletes conversation set to: " + comp);
                break;
            case "addoption":
                if (args.length < 8) {
                    player.sendMessage("Use: ... addoption <optId> <nextStageId> <text...>");
                    return;
                }

                String optId = args[5];
                String nextStage = args[6];
                String optText = getFullString(args, 7);

                if (findOption(stage, optId) != null) {
                    player.sendMessage("An option with that ID already exists.");
                    return;
                }

                Option newOption = new Option(optId, optText, nextStage, new ArrayList<>(), new ArrayList<>(), null);
                stage.getOptionList().add(newOption);
                player.sendMessage("Option '" + optId + "' created.");
                break;
            case "removeoption":
                if (args.length < 6) {
                    player.sendMessage("Use: ... removeoption <optId>");
                    return;
                }
                String remId = args[5];
                Option toRemove = findOption(stage, remId);
                if (toRemove != null) {
                    stage.getOptionList().remove(toRemove);
                    player.sendMessage("Option removed.");
                } else {
                    player.sendMessage("Option not found.");
                }
                break;

            case "modifyoption":
                handleOptionModification(player, stage, args);
                break;
            default:
                player.sendMessage("Unknown stage modification. Available: settext, addtext, removetext, setdelay, addaction, setcompletes, addoption, removeoption, modifyoption");
        }
    }

    private void handleOptionModification(Player player, ConversationStage stage, String[] args) {
        if (args.length < 7) {
            player.sendMessage("Use: ... modifyoption <optId> <action> <value>");
            player.sendMessage("Actions: settext, settarget, setnoreq, addaction, addreq");
            return;
        }

        String optId = args[5];
        Option option = findOption(stage, optId);

        if (option == null) {
            player.sendMessage("Option '" + optId + "' not found in this stage.");
            return;
        }

        String action = args[6].toLowerCase();

        switch (action) {
            case "settext":
                String text = getFullString(args, 7);
                option.setText(text);
                player.sendMessage("Option text updated.");
                break;

            case "settarget":
                if (args.length < 8) {
                    player.sendMessage("Specify the target stage ID.");
                    return;
                }
                option.setNextStageId(args[7]);
                player.sendMessage("Target stage updated to: " + args[7]);
                break;

            case "setnoreq":
                if (args.length < 8) {
                    player.sendMessage("Specify the no-requirement stage ID.");
                    return;
                }
                option.setNoRequirementId(args[7]);
                player.sendMessage("No-Requirement path updated to: " + args[7]);
                break;

            case "addaction":
                String actStr = getFullString(args, 7);
                Action act = ActionFactory.parse(actStr);
                if (act != null) {
                    option.getOnClickActions().add(act);
                    player.sendMessage("Action added to option.");
                } else {
                    player.sendMessage("Invalid action syntax.");
                }
                break;

            case "addreq":
                String reqStr = getFullString(args, 7);
                Requirement req = RequirementFactory.parse(reqStr, ComplexInteractions.getInstance().getItemManager(), "Requirement failed");
                if (req != null) {
                    option.getRequirements().add(req);
                    player.sendMessage("Requirement added to option.");
                } else {
                    player.sendMessage("Invalid requirement syntax.");
                }
                break;

            default:
                player.sendMessage("Unknown option action. Use: settext, settarget, setnoreq, addaction, addreq");
        }
    }

    private Option findOption(ConversationStage stage, String optId) {
        if (stage.getOptionList() == null) return null;

        for (Option opt : stage.getOptionList()) {
            if (opt.getId().equalsIgnoreCase(optId)) {
                return opt;
            }
        }
        return null;
    }
}
