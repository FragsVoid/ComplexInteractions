package org.frags.complexInteractions.objects.conversation.adapters;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;

public class CitizensHandler implements NpcAdapter {

    private NPC getCitizensNpc(String id) {
        try {
            int intId = Integer.parseInt(id);
            return CitizensAPI.getNPCRegistry().getById(intId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean isValid(ComplexInteractions plugin, String npcId) {
        return getCitizensNpc(npcId) != null;
    }

    @Override
    public Location getLocation(ComplexInteractions plugin, String npcId) {
        NPC npc = getCitizensNpc(npcId);
        return npc != null ? npc.getStoredLocation() : null;
    }

    @Override
    public void updateNpcLocation(ComplexInteractions plugin, String npcId, Location location) {
        NPC npc = getCitizensNpc(npcId);
        if (npc != null && npc.isSpawned()) {
            npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    @Override
    public String getNpcName(ComplexInteractions plugin, String npcId) {
        NPC npc = getCitizensNpc(npcId);
        return npc != null ? npc.getName() : npcId;
    }
}
