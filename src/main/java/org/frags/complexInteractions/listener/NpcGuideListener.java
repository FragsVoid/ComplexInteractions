package org.frags.complexInteractions.listener;

import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;

public class NpcGuideListener implements Listener {

    @EventHandler
    public void onChickenLayEgg(EntityDropItemEvent e) {
        if (!(e.getEntity() instanceof Chicken)) return;

        if (e.getItemDrop().getItemStack().getType() != Material.EGG) return;

        if (e.getEntity().getScoreboardTags().contains("npc_villager_interactions"))
            e.setCancelled(true);
    }
}
