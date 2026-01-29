package org.frags.complexInteractions.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.frags.complexInteractions.ComplexInteractions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private final ComplexInteractions plugin;

    public CooldownManager(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerCooldowns(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           Map<String, Long> loaded = plugin.getDatabase().getCooldowns(uuid);

           Bukkit.getScheduler().runTask(plugin, () -> {
              if (!loaded.isEmpty()) {
                  cooldowns.put(uuid, loaded);
              }
           });
        });
    }

    public void unloadPlayer(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public boolean isOnCooldown(UUID playerUUID, String cooldownId) {
        if (!cooldowns.containsKey(playerUUID)) return false;

        Map<String, Long> cooldown = cooldowns.get(playerUUID);
        Long expiry = cooldown.get(cooldownId);

        if (expiry == null) return false;

        if (System.currentTimeMillis() >= expiry) {
            cooldown.remove(cooldownId);
            if (cooldown.isEmpty()) cooldowns.remove(playerUUID);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatabase().removeCooldown(playerUUID, cooldownId));
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
        if (cooldowns.containsKey(playerUUID)) {
            Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
            playerCooldowns.remove(cooldownId);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerUUID);
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabase().removeCooldown(playerUUID, cooldownId);
        });
    }

    public void setCooldown(UUID playerUUID, String cooldownId, long seconds) {
        if (seconds <= 0) return;

        long expiry = System.currentTimeMillis() + (seconds * 1000);

        cooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                .put(cooldownId, expiry);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabase().setCooldown(playerUUID, cooldownId, expiry);
        });
    }

}
