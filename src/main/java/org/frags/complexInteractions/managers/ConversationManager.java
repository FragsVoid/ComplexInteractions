package org.frags.complexInteractions.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.files.InteractionFile;
import org.frags.complexInteractions.objects.Conversation;
import org.frags.complexInteractions.objects.ConversationStage;
import org.frags.complexInteractions.objects.Option;

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

    private void loadConversations() {
        conversations.clear();

        File folder = new File(plugin.getDataFolder(), "interactions");
        if (!folder.exists())
            folder.mkdirs();

        String[] files = folder.list();
        if (files == null)
            return;

        for (String fileName : files) {
            InteractionFile file = new InteractionFile(fileName, plugin);
            FileConfiguration config = file.getConfig();

            String npcId = config.getString("npc_id");
            boolean blockMovement = config.getBoolean("block_movement");
            boolean slowEffect = config.getBoolean("slow_effect");
            int startConversationRadius = config.getInt("start_conversation_radius");
            int endConversationRadius = config.getInt("end_conversation_radius");

            String startConversationId = config.getString("first_conversation");
            String noReqId = config.getString("no_requirement_conversation");
            String npcName = config.getString("npc_name");

            ConfigurationSection convSection = config.getConfigurationSection("conversation");


            Map<String, ConversationStage> conversationStages = new HashMap<>();
            for (String key : convSection.getKeys(false)) {
                ConfigurationSection section = convSection.getConfigurationSection(key);
                ConfigurationSection dialogueSection =  section.getConfigurationSection("dialogue");
                ConfigurationSection optionsSection =  section.getConfigurationSection("options");

                List<String> text = dialogueSection.getStringList("text");
                long delay = dialogueSection.getLong("delay");

                List<Option> optionList = new ArrayList<>();

                for (String optionKey : optionsSection.getKeys(false)) {
                    ConfigurationSection optionSection = optionsSection.getConfigurationSection(optionKey);
                    String optionText = optionSection.getString("text");
                    String startConversation = optionSection.getString("start_conversation");
                    optionList.add(new Option(optionKey, optionText, startConversation));
                }

                conversationStages.put(key, new ConversationStage(fileName, key, text, delay, optionList));
            }

            List<String> interruptActions = config.getStringList("interrupt_actions");

            List<String> requirements =  config.getStringList("requirements");

            long cooldown = config.getLong("cooldown");

            Conversation conversation =
                    new Conversation(fileName, npcId, blockMovement, slowEffect, startConversationRadius, endConversationRadius,
                            startConversationId, noReqId, npcName, conversationStages, interruptActions, requirements, cooldown);

            conversations.put(fileName, conversation);
        }
    }

}
