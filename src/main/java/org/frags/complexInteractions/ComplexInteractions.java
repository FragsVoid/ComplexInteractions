package org.frags.complexInteractions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.frags.complexInteractions.files.TemplateFile;
import org.frags.complexInteractions.managers.ConversationManager;
import org.frags.complexInteractions.managers.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ComplexInteractions extends JavaPlugin {

    private ConversationManager conversationManager;
    private SessionManager sessionManager;

    private static ComplexInteractions INSTANCE;

    private Map<String, String> cachedMessages = new HashMap<>();

    private TemplateFile messagesFile;
    private TemplateFile itemsFile;

    public static MiniMessage miniMessage = MiniMessage.miniMessage();

    private static Economy economy;

    @Override
    public void onEnable() {
        INSTANCE = this;

        loadConfig();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

    private void loadConfig() {
        this.messagesFile = new TemplateFile(this, "messages.yml");
        this.itemsFile = new TemplateFile(this, "items.yml");

        loadMessages();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

    private void loadMessages() {
        cachedMessages.clear();
        for (String key : messagesFile.getConfig().getKeys(false)) {
            String message = messagesFile.getConfig().getString(key);
            cachedMessages.put(key, message);
        }
    }

    public static ComplexInteractions getInstance() {
        return INSTANCE;
    }

    public String getMessage(String key) {
        return cachedMessages.get(key);
    }

    public ConversationManager getConversationManager() {
        return conversationManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
