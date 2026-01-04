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
import org.frags.complexInteractions.commands.MissionCommand;
import org.frags.complexInteractions.files.TemplateFile;
import org.frags.complexInteractions.listener.*;
import org.frags.complexInteractions.managers.*;
import org.frags.complexInteractions.menu.MenuManager;
import org.frags.complexInteractions.npcmovermanager.NpcAIMover;
import org.frags.complexInteractions.objects.conversation.adapters.CitizensHandler;
import org.frags.complexInteractions.objects.conversation.adapters.FancyNpcsHandler;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;

import java.util.*;

public final class ComplexInteractions extends JavaPlugin {

    private ConversationManager conversationManager;
    private SessionManager sessionManager;

    private WalkingManager walkingManager;

    private static ComplexInteractions INSTANCE;

    private CooldownManager cooldownManager;

    private Map<String, String> cachedMessages = new HashMap<>();

    private Map<String, Set<Player>> playersPerWorld = new HashMap<>();

    private TemplateFile messagesFile;
    private TemplateFile itemsFile;
    private TemplateFile dataFile;

    private MenuManager menuManager;

    private ConversationScanner conversationScanner;

    public static MiniMessage miniMessage = MiniMessage.miniMessage();

    public NpcAdapter npcAdapter;

    private static Economy economy;

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveResource("everyoption.txt", true);
        saveDefaultConfig();
        this.menuManager = new MenuManager(this);
        loadConfig();
        this.conversationManager = new ConversationManager(this);
        this.sessionManager = new SessionManager(conversationManager, this);
        this.walkingManager = new WalkingManager(this);
        if (getServer().getPluginManager().isPluginEnabled("Citizens")) {
            this.npcAdapter = new CitizensHandler();
            getServer().getPluginManager().registerEvents(new CitizensListener(this), this);
        } else if (getServer().getPluginManager().isPluginEnabled("FancyNpcs")) {
            this.npcAdapter = new FancyNpcsHandler();
            getServer().getPluginManager().registerEvents(new NpcInteractionListener(conversationManager, sessionManager), this);
            getServer().getPluginManager().registerEvents(new NpcSpawnListener(walkingManager), this);
            getServer().getPluginManager().registerEvents(new NpcDespawnListener(walkingManager), this);
        }

        walkingManager.setNpcAIMover(new NpcAIMover(this, npcAdapter));


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
        getCommand("mission").setExecutor(new MissionCommand(this));
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldGuardSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new NpcGuideListener(), this);
        getServer().getPluginManager().registerEvents(new ChangeWorldListener(this), this);

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
        menuManager.loadCaches();
        loadManagers();
    }

    private void loadManagers() {
        if (conversationManager != null) {
            conversationManager.reload();
        }

        if (walkingManager != null) {
            walkingManager.load(true);
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

    public void addPlayer(String world, Player player) {
        playersPerWorld.computeIfAbsent(world, k -> new HashSet<>()).add(player);
    }

    public void removePlayer(String world, Player player) {
        playersPerWorld.computeIfAbsent(world, k -> new HashSet<>()).remove(player);
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public Set<Player> getPlayersInWorld(String world) {
        return playersPerWorld.get(world);
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
