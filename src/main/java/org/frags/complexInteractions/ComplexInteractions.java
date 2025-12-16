package org.frags.complexInteractions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.frags.complexInteractions.commands.HeadCommand;
import org.frags.complexInteractions.commands.InteractionsCommand;
import org.frags.complexInteractions.files.TemplateFile;
import org.frags.complexInteractions.listener.*;
import org.frags.complexInteractions.managers.*;

import java.util.*;

public final class ComplexInteractions extends JavaPlugin {

    private ConversationManager conversationManager;
    private SessionManager sessionManager;
    private ItemManager itemManager;

    private WalkingManager walkingManager;

    private static ComplexInteractions INSTANCE;

    private CooldownManager cooldownManager;

    private Map<String, String> cachedMessages = new HashMap<>();

    private TemplateFile messagesFile;
    private TemplateFile itemsFile;
    private TemplateFile dataFile;

    private ConversationScanner conversationScanner;

    public static MiniMessage miniMessage = MiniMessage.miniMessage();

    private static Economy economy;

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveResource("everyoption.txt", true);

        loadConfig();

        this.itemManager = new ItemManager(this);

        this.conversationManager = new ConversationManager(this, itemManager);
        this.sessionManager = new SessionManager(conversationManager, this);
        this.walkingManager = new WalkingManager(this);

        if (conversationScanner != null) {
            conversationScanner.cancel();
            conversationScanner = null;
        }

        this.conversationScanner = new ConversationScanner(sessionManager, conversationManager);
        conversationScanner.runTaskTimer(this, 20L, 10L);

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("interactions").setExecutor(new InteractionsCommand(this));
        getCommand("interactions").setTabCompleter(new InteractionsCommand(this));
        getCommand("gethead").setExecutor(new HeadCommand(this));
        getServer().getPluginManager().registerEvents(new NpcInteractionListener(conversationManager, sessionManager), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new NpcSpawnListener(walkingManager), this);
        getServer().getPluginManager().registerEvents(new WorldGuardSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new NpcDespawnListener(walkingManager), this);
        getServer().getPluginManager().registerEvents(new ServerLoadListener(walkingManager), this);
        getServer().getPluginManager().registerEvents(new NpcGuideListener(), this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            cooldownManager.saveCooldowns();
            sessionManager.saveAllConversations();
            walkingManager.save();
        }, 12000L, 12000L);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (cooldownManager != null) {
            cooldownManager.saveCooldowns();
        }

        if (sessionManager != null) {
            sessionManager.saveAllConversations();
        }

        if (walkingManager != null) {
            walkingManager.getNpcAIMover().cancelAll();
            walkingManager.save();
        }
    }

    private void loadConfig() {
        this.messagesFile = new TemplateFile(this, "messages.yml");
        this.itemsFile = new TemplateFile(this, "items.yml");
        this.dataFile = new TemplateFile(this, "data.yml");

        this.cooldownManager = new CooldownManager(this);

        loadMessages();
    }

    public void reload() {
        reloadConfig();
        messagesFile.reloadConfig();
        itemsFile.reloadConfig();
        loadMessages();

        loadManagers();
    }

    private void loadManagers() {
        if (conversationManager != null) {
            conversationManager.reload();
        }

        if (walkingManager != null) {
            walkingManager.load(true);
        }

        if (itemManager != null) {
            itemManager.loadItems();
        }

        if (conversationScanner != null) {
            conversationScanner.cancel();
            conversationScanner = null;
        }

        this.conversationScanner = new ConversationScanner(sessionManager, conversationManager);
        conversationScanner.runTaskTimer(this, 20L, 10L);
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

    public WalkingManager getWalkingManager() {
        return walkingManager;
    }

    public TemplateFile getItemsFile() {
        return itemsFile;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
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

    public TemplateFile getDataFile() {
        return dataFile;
    }
}
