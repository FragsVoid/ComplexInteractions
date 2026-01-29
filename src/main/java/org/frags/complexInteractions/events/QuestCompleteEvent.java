package org.frags.complexInteractions.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuestCompleteEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private Player player;
    private String questId;
    private String missionId;

    public QuestCompleteEvent(Player player, String questId, String missionId) {
        this.player = player;
        this.questId = questId;
        this.missionId = missionId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getQuestId() {
        return questId;
    }

    public String getMissionId() {
        return missionId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
