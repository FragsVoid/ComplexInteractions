package org.frags.complexInteractions.listener;

import de.oliver.fancynpcs.api.events.NpcRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.managers.WalkingManager;

public class NpcDespawnListener implements Listener {

    private WalkingManager walkingManager;

    public NpcDespawnListener(WalkingManager walkingManager) {
        this.walkingManager = walkingManager;
    }

    @EventHandler
    public void onNpcDespawn(NpcRemoveEvent event) {
        String npcId = event.getNpc().getData().getId();

        Bukkit.getScheduler().runTask(ComplexInteractions.getInstance(), () -> {
            walkingManager.getNpcAIMover().cancelTask(npcId);
        });

    }
}
