package org.frags.complexInteractions.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.files.InteractionFile;
import org.frags.complexInteractions.objects.conversation.*;
import org.frags.complexInteractions.objects.conversation.factories.ActionFactory;
import org.frags.complexInteractions.objects.conversation.factories.RequirementFactory;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationManager {

    private ComplexInteractions plugin;

    private Map<String, Conversation> conversations;

    public ConversationManager(ComplexInteractions plugin) {
        this.plugin = plugin;

        conversations = new HashMap<>();
        loadConversations();
    }

    public Conversation getConversation(String id) {
        return conversations.get(id);
    }

    private void loadConversations() {
        conversations.clear();

        File folder = new File(plugin.getDataFolder(), "interactions");
        if (!folder.exists())
            folder.mkdirs();

        String[] files = folder.list();
        if (files == null)
            return;

        ItemProvider itemProvider = null;

        for (String fileName : files) {
            String conversationId = fileName.replace(".yml", "");

            InteractionFile file = new InteractionFile(conversationId, plugin);
            FileConfiguration config = file.getConfig();

            String npcId = config.getString("npc_id");
            boolean blockMovement = config.getBoolean("block_movement");
            boolean slowEffect = config.getBoolean("slow_effect");
            int startRadius = config.getInt("start_conversation_radius");
            int endRadius = config.getInt("end_conversation_radius");

            String startStageId = config.getString("first_conversation");
            String noReqStageId = config.getString("no_requirement_conversation");
            String npcName = config.getString("npc_name");
            long cooldown =  config.getLong("cooldown");

            ConfigurationSection convSection = config.getConfigurationSection("conversation");
            Map<String, ConversationStage> conversationStages = new HashMap<>();

            for (String stageKey : convSection.getKeys(false)) {
                ConfigurationSection section = convSection.getConfigurationSection(stageKey);
                ConfigurationSection dialogueSection =  section.getConfigurationSection("dialogue");
                ConfigurationSection optionsSection =  section.getConfigurationSection("options");

                List<String> text = dialogueSection.getStringList("text");
                long delay = dialogueSection.getLong("delay");

                List<Option> optionList = new ArrayList<>();

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

                    optionList.add(new Option(optionKey, optionText, startConversation, parsedActions, parsedRequirements));
                }

                conversationStages.put(stageKey, new ConversationStage(conversationId, stageKey, text, delay, optionList));
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


            Conversation conversation = new Conversation(
                    conversationId, npcId, blockMovement, slowEffect, startRadius, endRadius, startStageId, noReqStageId,
                    npcName, conversationStages, interruptActions, globalRequirements, cooldown
            );
            conversations.put(npcId, conversation);
        }
    }

}
