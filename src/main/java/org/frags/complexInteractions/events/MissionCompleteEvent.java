package org.frags.complexInteractions.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MissionCompleteEvent extends Event {

    private Player player;
    private String missionId;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public MissionCompleteEvent(Player player, String missionId) {
        this.player = player;
        this.missionId = missionId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMissionId() {
        return missionId;
    }
}
