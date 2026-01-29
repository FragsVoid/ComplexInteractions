package org.frags.complexInteractions.listener;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.frags.complexInteractions.events.QuestCompleteEvent;
import org.frags.complexInteractions.objects.missions.ActiveQuest;
import org.frags.complexInteractions.managers.QuestManager;

import java.util.Map;

public class QuestListener implements Listener {

    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onMobKill (MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player killer)) return;

        Map<String, ActiveQuest> quests = questManager.getQuests(killer);
        if (quests == null || quests.isEmpty()) return;

        for (ActiveQuest quest : quests.values()) {
            if (quest.getMobProgress(event.getMobType().getInternalName()) != null) {
                if (quest.isComplete()) continue;

                quest.addProgress(event.getMobType().getInternalName());

                if (quest.isComplete()) {
                    Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(killer, quest.getQuestId(), quest.getNpcId()));
                }
            }
        }

    }
}
