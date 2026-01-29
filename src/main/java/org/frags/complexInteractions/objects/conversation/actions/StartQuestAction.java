package org.frags.complexInteractions.objects.conversation.actions;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Action;
import org.frags.complexInteractions.objects.missions.ActiveQuest;
import org.frags.complexInteractions.objects.missions.MobProgress;

import java.util.List;
import java.util.Map;

public class StartQuestAction extends Action {

    private final ComplexInteractions plugin;

    private String npcId;
    private String questId;
    private Map<String, MobProgress> mobProgress;

    public StartQuestAction(ComplexInteractions plugin, String npcId, String questId, Map<String, MobProgress> mobProgress) {
        this.plugin = plugin;
        this.npcId = npcId;
        this.questId = questId;
        this.mobProgress = mobProgress;
    }

    @Override
    public boolean execute(Player player) {
        ActiveQuest quest = new ActiveQuest(npcId, questId, mobProgress);
        plugin.getQuestManager().startQuest(player, quest);
        return true;
    }
}
