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
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.walking.WalkingObject;

import java.util.Set;
import java.util.UUID;

public class NpcSpawnListener implements Listener {


    private final ComplexInteractions plugin;

    public NpcSpawnListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNpcSpawn(NpcSpawnEvent event) {
        String npcId = event.getNpc().getData().getName();

        if (npcId.contains("_g_")) {
            UUID creatorId = event.getNpc().getData().getCreator();

            if (!event.getPlayer().getUniqueId().equals(creatorId)) event.setCancelled(true);

            return;
        }
        Conversation conversation = plugin.getConversationManager().getConversation(npcId);
        if (conversation != null) {
            Session session = plugin.getSessionManager().getSession(event.getPlayer());
            if (session != null) {
                if (session.getConversation().getId().equals(conversation.getId()) && session.isWaitingForMovement()) {
                    event.setCancelled(true);
                }
            }

            if (!conversation.meetsRequirement(event.getPlayer())) {
                event.setCancelled(true);
            }

            if (plugin.containsNpcs(npcId)) {
                if (plugin.getSessionManager().hasCompleted(event.getPlayer().getUniqueId(), conversation)) {
                    event.setCancelled(true);
                }
            }
        }


        WalkingObject walkingObject = plugin.getWalkingManager().getWalkingByNpcId(npcId);
        if (walkingObject == null) return;

        Bukkit.getScheduler().runTask(ComplexInteractions.getInstance(), () -> {
            plugin.getWalkingManager().getNpcAIMover().startWalkingTask(npcId);
        });
    }
}
