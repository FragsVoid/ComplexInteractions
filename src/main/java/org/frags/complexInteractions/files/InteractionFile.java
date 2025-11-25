package org.frags.complexInteractions.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frags.complexInteractions.ComplexInteractions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Level;

public class InteractionFile {

    private ComplexInteractions plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;
    private String name;


    public InteractionFile(String id, ComplexInteractions plugin) {
        this.plugin = plugin;
        this.name = "interactions/" + id + ".yml";
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(plugin.getDataFolder(), name);

        dataConfig = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource(name);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.
                    loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null)
            reloadConfig();
        return dataConfig;
    }

    public void saveConfig() {
        if (dataConfig == null || configFile == null)
            return;

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar " +
                    this.configFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null)
            this.configFile = new File(plugin.getDataFolder(), name);

        if (!configFile.exists()) {
            try {
                if (configFile.getParentFile() != null) {
                    configFile.getParentFile().mkdirs();
                }

                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create interaction file");
                e.printStackTrace();
            }

        }
    }


    public void resetFile() {
        configFile.delete();
        saveDefaultConfig();
    }
}
