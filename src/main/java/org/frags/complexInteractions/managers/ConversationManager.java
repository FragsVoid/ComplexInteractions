package org.frags.complexInteractions.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BoundingBox;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.files.InteractionFile;
import org.frags.complexInteractions.objects.conversation.*;
import org.frags.complexInteractions.objects.conversation.factories.ActionFactory;
import org.frags.complexInteractions.objects.conversation.factories.RequirementFactory;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemProvider;
import org.frags.complexInteractions.objects.walking.WalkingObject;
import org.frags.complexInteractions.objects.walking.Waypoints;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationManager {

    private ComplexInteractions plugin;
    private ItemProvider itemProvider;

    private Map<String, Conversation> conversations;

    public ConversationManager(ComplexInteractions plugin, ItemProvider itemProvider) {
        this.plugin = plugin;
        this.itemProvider = itemProvider;

        conversations = new HashMap<>();
        loadConversations();
    }

    public void addConversation(String npcId, Conversation conversation) {
        conversations.put(npcId, conversation);
    }

    public boolean fileExists(String file) {
        for (Conversation c : conversations.values()) {
            if (c.getId().equalsIgnoreCase(file)) {
                return true;
            }
        }
        return false;
    }

    public Conversation getConversation(String id) {
        return conversations.get(id);
    }

    public List<Conversation> getAllConversations() {
        return new ArrayList<>(conversations.values());
    }

    public void reload() {
        loadConversations();
    }

    public List<String> getAllNpcsId() {
        List<String> ids = new ArrayList<>();
        for (Conversation c : conversations.values()) {
            ids.add(c.getNpcId());
        }
        return ids;
    }

    private void loadConversations() {
        conversations.clear();

        File folder = new File(plugin.getDataFolder(), "interactions");
        if (!folder.exists()) {
            try {
                plugin.saveResource("interactions/example.yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Didn't find example.yml");
            }
        }

        String[] files = folder.list();
        if (files == null)
            return;

        for (String fileName : files) {
            String conversationId = fileName.replace(".yml", "");

            File targetFile = new File(folder, fileName);
            FileConfiguration config = YamlConfiguration.loadConfiguration(targetFile);

            String npcId = config.getString("npc_id");
            boolean blockMovement = config.getBoolean("block_movement");
            boolean slowEffect = config.getBoolean("slow_effect");
            int startRadius = config.getInt("start_conversation_radius");
            int endRadius = config.getInt("end_conversation_radius");

            String startStageId = config.getString("first_conversation");
            String noReqStageId = config.getString("no_requirement_conversation");
            String npcName = config.getString("npc_name");
            long cooldown =  config.getLong("cooldown");

            String cooldownStageId = config.getString("cooldown_conversation");

            ConfigurationSection convSection = config.getConfigurationSection("conversation");
            Map<String, ConversationStage> conversationStages = new HashMap<>();

            for (String stageKey : convSection.getKeys(false)) {
                ConfigurationSection section = convSection.getConfigurationSection(stageKey);
                ConfigurationSection dialogueSection =  section.getConfigurationSection("dialogue");
                ConfigurationSection optionsSection =  section.getConfigurationSection("options");

                String completesConversation = section.getString("completes_conversation");

                List<String> text = dialogueSection.getStringList("text");
                long delay = dialogueSection.getLong("delay");

                List<Option> optionList = new ArrayList<>();
                if (optionsSection != null) {
                    for (String optionKey : optionsSection.getKeys(false)) {
                        ConfigurationSection optionSection = optionsSection.getConfigurationSection(optionKey);

                        String optionText = optionSection.getString("text");
                        String startConversation = optionSection.getString("start_conversation");

                        List<String> rawActions = optionSection.getStringList("actions");
                        List<Action> parsedActions = new ArrayList<>();

                        String failMessage = optionSection.getString("fail_message");

                        for (String line : rawActions) {
                            Action act = ActionFactory.parse(line);
                            if (act != null)
                                parsedActions.add(act);
                        }

                        List<Requirement> parsedRequirements = new ArrayList<>();
                        for (String line : optionSection.getStringList("requirements")) {
                            Requirement req = RequirementFactory.parse(line, itemProvider, failMessage);
                            if (req != null)
                                parsedRequirements.add(req);
                        }

                        String noRequirementId = optionSection.getString("no_requirement");

                        optionList.add(new Option(optionKey, optionText, startConversation, parsedActions, parsedRequirements, noRequirementId));
                    }
                }
                List<String> rawActions = section.getStringList("actions");
                List<Action> actions = new ArrayList<>();
                for (String actionKey : rawActions) {
                    Action action = ActionFactory.parse(actionKey);
                    if (action != null)
                        actions.add(action);
                }

                conversationStages.put(stageKey, new ConversationStage(conversationId, stageKey, text, delay, actions, optionList, completesConversation));
            }

            List<Action> interruptActions = new ArrayList<>();
            for (String line : config.getStringList("interrupt_actions")) {
                Action act = ActionFactory.parse(line);
                if (act != null)
                    interruptActions.add(act);
            }

            String failMessage = config.getString("fail_message");

            List<Requirement> globalRequirements = new ArrayList<>();
            for (String line : config.getStringList("requirements")) {
                Requirement req = RequirementFactory.parse(line, itemProvider, failMessage);
                if (req != null)
                    globalRequirements.add(req);
            }

            boolean onlyOnce = config.getBoolean("only_once");
            String alreadyCompletedStageId = config.getString("already_completed_id");


            Conversation conversation = new Conversation(
                    conversationId, npcId, blockMovement, slowEffect, startRadius, endRadius, startStageId, noReqStageId,
                    npcName, conversationStages, interruptActions, globalRequirements, cooldown, cooldownStageId, onlyOnce,
                    alreadyCompletedStageId
            );
            conversations.put(npcId, conversation);
        }
    }

    public void save() {
        File folder = new File(plugin.getDataFolder(), "interactions");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        List<String> currentIds = new ArrayList<>();

        for (Conversation conversation : conversations.values()) {
            String conversationId = conversation.getId();
            currentIds.add(conversationId);

            File file = new File(folder, conversationId + ".yml");
            try {
                if (!file.exists()) {
                    if (!file.createNewFile())
                        file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            for (String key : config.getKeys(false)) {
                config.set(key, null);
            }

            config.set("npc_id", conversation.getNpcId());
            config.set("block_movement", conversation.isBlockMovement());
            config.set("slow_effect", conversation.isSlowEffect());
            config.set("start_conversation_radius", conversation.getStarConversationRadius());
            config.set("end_conversation_radius", conversation.getEndConversationRadius());

            config.set("first_conversation", conversation.getStartStageId());
            config.set("no_requirement_conversation", conversation.getNoReqStageId());
            config.set("npc_name", conversation.getNpcName());

            config.set("cooldown", conversation.getCooldown());
            config.set("cooldown_conversation", conversation.getCooldownMessage());

            config.set("only_once", conversation.isOnlyOnce());
            config.set("already_completed_id", conversation.getAlreadyCompletedStageId());

            config.set("fail_message", "You cannot talk to this NPC yet.");

            List<String> globalReqs = new ArrayList<>();
            for (Requirement req : conversation.getRequirements()) {
                globalReqs.add(req.toString());
            }
            config.set("requirements", globalReqs);

            List<String> interruptActions = new ArrayList<>();
            for (Action action : conversation.getInterruptActions()) {
                interruptActions.add(action.toString());
            }
            config.set("interrupt_actions", interruptActions);

            ConfigurationSection convSection = config.createSection("conversation");

            for (Map.Entry<String, ConversationStage> stageEntry : conversation.getConversationStageMap().entrySet()) {
                String stageKey = stageEntry.getKey();
                ConversationStage stage = stageEntry.getValue();

                ConfigurationSection stageSection = convSection.createSection(stageKey);

                stageSection.set("completes_conversation", stage.getCompletesConversation());

                List<String> stageActions = new ArrayList<>();
                for (Action act : stage.getActions()) {
                    stageActions.add(act.toString());
                }
                stageSection.set("actions", stageActions);

                ConfigurationSection dialogueSection = stageSection.createSection("dialogue");
                dialogueSection.set("text", stage.getText());
                dialogueSection.set("delay", stage.getDelay());

                List<Option> options = stage.getOptionList();
                if (options != null && !options.isEmpty()) {
                    ConfigurationSection optionsSection = stageSection.createSection("options");

                    for (Option option : options) {
                        ConfigurationSection optSection = optionsSection.createSection(option.getId());

                        optSection.set("text", option.getText());
                        optSection.set("start_conversation", option.getNextStage());
                        optSection.set("no_requirement", option.getNoRequirementId());

                        List<String> optActions = new ArrayList<>();
                        for (Action act : option.getOnClickActions()) {
                            optActions.add(act.toString());
                        }
                        optSection.set("actions", optActions);

                        List<String> optReqs = new ArrayList<>();
                        for (Requirement req : option.getRequirements()) {
                            optReqs.add(req.toString());
                        }
                        optSection.set("requirements", optReqs);
                    }
                }
            }



            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

}
