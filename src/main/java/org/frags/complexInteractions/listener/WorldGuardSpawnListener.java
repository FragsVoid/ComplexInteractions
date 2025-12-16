package org.frags.complexInteractions.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()) return;

        if (event.getEntity().getScoreboardTags().contains("npc_villager_interactions")) {
            event.setCancelled(false);
        }
    }
}
