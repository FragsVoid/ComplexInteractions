package org.frags.complexInteractions.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.frags.complexInteractions.ComplexInteractions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    private final ComplexInteractions plugin;

    public CooldownManager(ComplexInteractions plugin) {
        this.plugin = plugin;

        loadCooldowns();
    }

    public boolean isOnCooldown(UUID playerUUID, String cooldownId) {
        if (!cooldowns.containsKey(playerUUID)) return false;

        Map<String, Long> cooldown = cooldowns.get(playerUUID);

        Long expiry = cooldown.get(cooldownId);
        if (expiry == null) return false;

        if (System.currentTimeMillis() >= expiry) {
            cooldown.remove(cooldownId);
            if (cooldown.isEmpty()) cooldowns.remove(playerUUID);
            return false;
        }

        return true;
    }

    public long getRemainingSeconds(UUID playerUUID,  String cooldownId) {
        if (!isOnCooldown(playerUUID, cooldownId)) return 0;
        long diff = cooldowns.get(playerUUID).get(cooldownId) - System.currentTimeMillis();
        return diff / 1000;
    }

    public void resetCooldown(UUID playerUUID, String cooldownId) {
        Map<String, Long> cooldown = cooldowns.get(playerUUID);
        if (cooldown == null) return;
        cooldown.remove(cooldownId);
        if (cooldown.isEmpty()) cooldowns.remove(playerUUID);
    }

    public void setCooldown(UUID playerUUID, String cooldownId, long seconds) {
        if (seconds <= 0) return;

        cooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>())
                .put(cooldownId, System.currentTimeMillis() + seconds * 1000);
    }

    public void saveCooldowns() {
        if (cooldowns.isEmpty()) return;

        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            String uuidString = entry.getKey().toString();
            Map<String, Long> playerCooldowns = entry.getValue();

            for (Map.Entry<String, Long> cooldownEntry : playerCooldowns.entrySet()) {
                String cooldownId = cooldownEntry.getKey();
                long expiry = cooldownEntry.getValue();

                if (expiry > now) {
                    plugin.getDataFile().getConfig().set("cooldowns." + uuidString + "." + cooldownId, expiry);
                }
            }
        }

        plugin.getDataFile().saveConfig();
    }

    public void saveCooldownsAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveCooldowns);
    }

    private void loadCooldowns() {
        ConfigurationSection section = plugin.getDataFile().getConfig().getConfigurationSection("cooldowns");

        if (section == null) return;

        long now = System.currentTimeMillis();

        for (String uuidString : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                ConfigurationSection playerSection = section.getConfigurationSection(uuidString);

                if (playerSection == null) continue;

                for (String cooldownId : playerSection.getKeys(false)) {
                    long expiry = playerSection.getLong(cooldownId);

                    if (expiry > now) {
                        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                                .put(cooldownId, expiry);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid cooldown id '" + uuidString + "'");
            }
        }
    }
}
