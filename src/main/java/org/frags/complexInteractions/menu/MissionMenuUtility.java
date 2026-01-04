package org.frags.complexInteractions.menu;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.MissionFilter;
import org.frags.customItems.menu.PlayerMenuUtility;

public class MissionMenuUtility extends PlayerMenuUtility {

    private MissionFilter missionFilter = MissionFilter.ALL;

    public MissionMenuUtility(Player player) {
        super(player);
    }

    public MissionFilter getMissionFilter() {
        return missionFilter;
    }

    public void setMissionFilter(MissionFilter missionFilter) {
        this.missionFilter = missionFilter;
    }
}
