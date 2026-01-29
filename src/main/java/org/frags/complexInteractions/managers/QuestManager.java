package org.frags.complexInteractions.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.missions.ActiveQuest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final Map<UUID, Map<String, ActiveQuest>> activeQuests = new ConcurrentHashMap<>();
    private final ComplexInteractions plugin;

    public QuestManager(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerData(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, ActiveQuest> loaded = plugin.getDatabase().loadActiveQuests(uuid);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (loaded != null && !loaded.isEmpty()) {
                    activeQuests.put(uuid, loaded);
                }
            });
        });
    }

    public void savePlayerData(UUID uuid) {
        Map<String, ActiveQuest> quests = activeQuests.get(uuid);
        if (quests == null || quests.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (ActiveQuest activeQuest : quests.values()) {
                plugin.getDatabase().saveActiveQuest(uuid, activeQuest);
            }
        });

        activeQuests.remove(uuid);
    }

    public void saveAllData() {
        for (Map.Entry<UUID, Map<String, ActiveQuest>> entry : activeQuests.entrySet()) {
            Map<String, ActiveQuest> quests = entry.getValue();
            if (quests == null || quests.isEmpty()) continue;

            for (ActiveQuest activeQuest : quests.values()) {
                plugin.getDatabase().saveActiveQuest(entry.getKey(), activeQuest);
            }
        }
    }

    public boolean startQuest(Player player, ActiveQuest activeQuest) {
        UUID uuid = player.getUniqueId();
        activeQuests.putIfAbsent(uuid, new HashMap<>());
        Map<String, ActiveQuest> quests = activeQuests.get(uuid);

        if (quests.containsKey(activeQuest.getQuestId())) {
            return false;
        }

        if (quests.size() >= 5) return false;

        quests.put(activeQuest.getQuestId(), activeQuest);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabase().saveActiveQuest(uuid, activeQuest);
        });

        return true;
    }

    public Map<String, ActiveQuest> getQuests(Player player) {
        return activeQuests.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public void removeQuest(Player player, String questId) {
        UUID uuid = player.getUniqueId();
        if (activeQuests.containsKey(uuid)) {
            activeQuests.get(uuid).remove(questId);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDatabase().deleteActiveQuest(uuid, questId);
            });
        }
    }
}
