package org.frags.complexInteractions.menu;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.menu.menucache.DefaultCache;
import org.frags.complexInteractions.menu.menucache.MissionCache;

public class MenuManager {

    private final ComplexInteractions plugin;

    private final DefaultCache defaultCache;
    private final MissionCache missionCache;

    public MenuManager(ComplexInteractions plugin) {
        this.plugin = plugin;
        this.defaultCache = new DefaultCache();
        this.missionCache = new MissionCache();

        loadCaches();
    }

    public void loadCaches() {
        var config = plugin.getConfig();

        defaultCache.load(config.getConfigurationSection("default"));
        missionCache.load(config.getConfigurationSection("mission"));
    }

    public DefaultCache getDefaultCache() {
        return defaultCache;
    }

    public MissionCache getMissionCache() {
        return missionCache;
    }
}
