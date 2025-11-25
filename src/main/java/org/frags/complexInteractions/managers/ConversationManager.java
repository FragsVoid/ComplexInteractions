package org.frags.complexInteractions.managers;

import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.files.InteractionFile;
import org.frags.complexInteractions.objects.Conversation;

import java.io.File;
import java.util.HashMap;
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
        }

    }

}
