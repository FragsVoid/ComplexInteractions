package org.frags.complexInteractions.objects.conversation.interfaces;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;

public interface NpcAdapter {

    boolean isValid(ComplexInteractions plugin, String npcId);

    Location getLocation(ComplexInteractions plugin, String npcId);

    void updateNpcLocation(ComplexInteractions plugin, String npcId, Location location);

    String getNpcName(ComplexInteractions plugin, String npcId);

    void hideNpcForPlayer(String npcId, Player player);

    void showNpcForPlayer(String npcId, Player player);

    Npc createGhostNpc(String originalId, Player player);

    void removeNpc(Npc npc);

    void updateNpcInstanceLocation(Npc npc, Location location);
}
