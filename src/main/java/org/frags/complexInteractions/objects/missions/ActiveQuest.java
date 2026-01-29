package org.frags.complexInteractions.objects.missions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ActiveQuest {

    private String npcId;
    private String questId;
    private Map<String, MobProgress> mobList;

    public ActiveQuest(String npcId, String questId, Map<String, MobProgress> targetMobList) {
        this.npcId = npcId;
        this.questId = questId;
        this.mobList = targetMobList;
    }

    public void addProgress(String mobType) {
        MobProgress mobProgress = mobList.get(mobType);
        if (mobProgress == null) return;
        mobProgress.addProgress();
    }

    public boolean isComplete() {
        for (MobProgress mobProgress : mobList.values()) {
            if (!mobProgress.isComplete())
                return false;
        }

        return true;
    }

    public Map<String, MobProgress> getMobList() {
        return mobList;
    }

    public String getNpcId() {
        return npcId;
    }

    public String getQuestId() {
        return questId;
    }

    @Nullable
    public MobProgress getMobProgress(String mobType) {
        return mobList.get(mobType);
    }
}
