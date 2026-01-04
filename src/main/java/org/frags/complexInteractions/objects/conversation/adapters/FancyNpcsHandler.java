package org.frags.complexInteractions.objects.conversation.adapters;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;

public class FancyNpcsHandler implements NpcAdapter {

    @Override
    public boolean isValid(ComplexInteractions plugin, String npcId) {
        return FancyNpcsPlugin.get().getNpcManager().getNpc(npcId) != null;
    }

    @Override
    public Location getLocation(ComplexInteractions plugin, String npcId) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        return npc != null ? npc.getData().getLocation() : null;
    }

    @Override
    public void updateNpcLocation(ComplexInteractions plugin, String npcId, Location location) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        if (npc == null) return;

        npc.getData().setLocation(location);

        for (Player p : plugin.getPlayersInWorld(location.getWorld().getName())) {
            npc.move(p, false);
        }
    }

    @Override
    public String getNpcName(ComplexInteractions plugin, String npcId) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        return npc != null ? npc.getData().getName() : npcId;
    }
}
