package org.frags.complexInteractions.listener;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcRemoveEvent;
import de.oliver.fancynpcs.api.events.NpcSpawnEvent;
import de.oliver.fancynpcs.api.events.NpcsLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.managers.WalkingManager;
import org.frags.complexInteractions.objects.walking.WalkingObject;

public class NpcSpawnListener implements Listener {

    private WalkingManager walkingManager;

    public NpcSpawnListener(WalkingManager walkingManager) {
        this.walkingManager = walkingManager;
    }

    @EventHandler
    public void onNpcSpawn(NpcSpawnEvent event) {
        String npcId = event.getNpc().getData().getName();

        WalkingObject walkingObject = walkingManager.getWalkingByNpcId(npcId);
        if (walkingObject == null) return;

        Bukkit.getScheduler().runTask(ComplexInteractions.getInstance(), () -> {
            walkingManager.getNpcAIMover().startWalkingTask(npcId);
        });
    }
}
