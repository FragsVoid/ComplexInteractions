package org.frags.complexInteractions.objects.conversation.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;

public interface NpcAdapter {

    boolean isValid(ComplexInteractions plugin, String npcId);

    Location getLocation(ComplexInteractions plugin, String npcId);

    void updateNpcLocation(ComplexInteractions plugin, String npcId, Location location);

    String getNpcName(ComplexInteractions plugin, String npcId);
}
