package org.frags.complexInteractions;

import io.lumine.mythic.bukkit.utils.storage.sql.hikari.HikariConfig;
import io.lumine.mythic.bukkit.utils.storage.sql.hikari.HikariDataSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.frags.complexInteractions.commands.HeadCommand;
import org.frags.complexInteractions.commands.InteractionsCommand;
import org.frags.complexInteractions.commands.MissionCommand;
import org.frags.complexInteractions.commands.QuestCommand;
import org.frags.complexInteractions.data.SQLDatabase;
import org.frags.complexInteractions.files.TemplateFile;
import org.frags.complexInteractions.listener.*;
import org.frags.complexInteractions.managers.*;
import org.frags.complexInteractions.menu.MenuManager;
import org.frags.complexInteractions.npcmovermanager.NpcAIMover;
import org.frags.complexInteractions.objects.DataStorage;
import org.frags.complexInteractions.objects.conversation.adapters.FancyNpcsHandler;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;

import java.io.File;
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

    private Set<String> npcsToDissapear;

    private DataStorage database;

    private MenuManager menuManager;

    private ConversationScanner conversationScanner;

    private QuestManager questManager;

    public static MiniMessage miniMessage = MiniMessage.miniMessage();

    public NpcAdapter npcAdapter;

    private static Economy economy;

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveResource("everyoption.txt", true);
        saveDefaultConfig();
        if (!setupDatabase()) {
            getLogger().severe("Database not found! Disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().info("Database found!");
        }
        this.menuManager = new MenuManager(this);
        loadConfig();
        this.conversationManager = new ConversationManager(this);
        this.sessionManager = new SessionManager(conversationManager, this);
        this.walkingManager = new WalkingManager(this);
        this.questManager = new QuestManager(this);
        if (getServer().getPluginManager().isPluginEnabled("Citizens")) {
            getServer().getPluginManager().registerEvents(new CitizensListener(this), this);
        } else if (getServer().getPluginManager().isPluginEnabled("FancyNpcs")) {
            this.npcAdapter = new FancyNpcsHandler();
            getServer().getPluginManager().registerEvents(new NpcInteractionListener(conversationManager, sessionManager), this);
            getServer().getPluginManager().registerEvents(new NpcSpawnListener(this), this);
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

        loadNpcs();

        getCommand("interactions").setExecutor(new InteractionsCommand(this));
        getCommand("interactions").setTabCompleter(new InteractionsCommand(this));
        getCommand("gethead").setExecutor(new HeadCommand(this));
        getCommand("mission").setExecutor(new MissionCommand(this));
        getCommand("quest").setExecutor(new QuestCommand());
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldGuardSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new NpcGuideListener(), this);
        getServer().getPluginManager().registerEvents(new ChangeWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestListener(questManager), this);
        getServer().getPluginManager().registerEvents(new QuestCompleteListener(this), this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            walkingManager.save();
            getQuestManager().saveAllData();
        }, 12000L, 12000L);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Player player : getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            getSessionManager().endSession(player, false);
            getSessionManager().unloadPlayer(uuid);
            getCooldownManager().unloadPlayer(uuid);
        }

        if (walkingManager != null) {
            walkingManager.getNpcAIMover().cancelAll();
            walkingManager.save();
        }
    }

    public void loadNpcs() {
        npcsToDissapear = new HashSet<>(getConfig().getStringList("npc_to_dissapear"));
    }

    public boolean setupDatabase() {
        boolean useMySQL = getConfig().getBoolean("use_mysql");
        HikariConfig config = new HikariConfig();

        if (useMySQL) {
            String host = getConfig().getString("mysql.host");
            String port = getConfig().getString("mysql.port");
            String dbName = getConfig().getString("mysql.database");
            String username = getConfig().getString("mysql.username");
            String password = getConfig().getString("mysql.password");
            boolean useSSL = getConfig().getBoolean("mysql.useSSL");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=" + useSSL);
            config.setUsername(username);
            config.setPassword(password);
        } else {
            File file = new File(getDataFolder(), "skins.db");

            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            config.setConnectionTestQuery("SELECT 1");
            config.setMaxLifetime(60000);
            config.setIdleTimeout(45000);
            config.setMaximumPoolSize(1);
        }

        if (useMySQL) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }

        try {
            HikariDataSource ds = new HikariDataSource(config);
            this.database = new SQLDatabase(ds, useMySQL);
            this.database.init();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public DataStorage getDatabase() {
        return database;
    }

    private void loadConfig() {
        this.messagesFile = new TemplateFile(this, "messages.yml");
        this.itemsFile = new TemplateFile(this, "items.yml");

        this.cooldownManager = new CooldownManager(this);

        loadMessages();
    }

    public boolean containsNpcs(String npcId) {
        return npcsToDissapear.contains(npcId);
    }

    public NpcAdapter getNpcAdapter() {
        return npcAdapter;
    }

    public void reload() {
        reloadConfig();
        messagesFile.reloadConfig();
        itemsFile.reloadConfig();
        loadMessages();
        menuManager.loadCaches();
        loadManagers();
        loadNpcs();
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

    public QuestManager getQuestManager() {
        return questManager;
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

}
