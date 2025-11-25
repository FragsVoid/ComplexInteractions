package org.frags.complexInteractions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.frags.complexInteractions.managers.ConversationManager;

public final class ComplexInteractions extends JavaPlugin {

    private ConversationManager conversationManager;

    public static MiniMessage miniMessage = MiniMessage.miniMessage();


    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

    public ConversationManager getConversationManager() {
        return conversationManager;
    }
}
