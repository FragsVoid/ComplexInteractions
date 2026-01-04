package org.frags.complexInteractions.listener;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.walking.WalkingObject;

public class CitizensListener implements Listener {

    private final ComplexInteractions plugin;

    public CitizensListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(NPCClickEvent e) {
        String nameId = String.valueOf(e.getNPC().getId());

        plugin.getSessionManager().startSession(e.getClicker(), nameId);
    }

    @EventHandler
    public void onSpawn(NPCSpawnEvent e) {
        String npcId = String.valueOf(e.getNPC().getId());

        WalkingObject walkingObject = plugin.getWalkingManager().getWalkingByNpcId(npcId);
        if (walkingObject == null) return;

        Bukkit.getScheduler().runTask(ComplexInteractions.getInstance(), () -> plugin.getWalkingManager().getNpcAIMover().startWalkingTask(npcId));
    }
}
