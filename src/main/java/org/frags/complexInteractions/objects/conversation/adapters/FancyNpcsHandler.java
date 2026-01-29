package org.frags.complexInteractions.objects.conversation.adapters;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.events.NpcCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    @Override
    public void hideNpcForPlayer(String npcId, Player player) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        if (npc != null) {
            npc.remove(player);
        }
    }

    @Override
    public void showNpcForPlayer(String npcId, Player player) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        if (npc != null) {
            npc.spawn(player);
        }
    }

    @Override
    public Npc createGhostNpc(String originalId, Player player) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(originalId);
        if (npc == null) return null;

        String name = originalId + "_g_" + player.getUniqueId().toString().substring(0, 8);
        NpcData ghostData = new NpcData(
                UUID.randomUUID().toString(),
                name,
                player.getUniqueId(),
                npc.getData().getDisplayName(),
                npc.getData().getSkinData(),
                npc.getData().getLocation(),
                false,
                true,
                false,
                npc.getData().isGlowing(),
                npc.getData().getGlowingColor(),
                npc.getData().getType(),
                new ConcurrentHashMap<>(npc.getData().getEquipment()),
                false,
                npc.getData().getTurnToPlayerDistance(),
                npc.getData().getOnClick(),
                npc.getData().getActions()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toConcurrentMap(
                                Map.Entry::getKey,
                                e -> new ArrayList<>(e.getValue())
                        )),
                npc.getData().getInteractionCooldown(),
                npc.getData().getScale(),
                200,
                new ConcurrentHashMap<>(npc.getData().getAttributes()),
                npc.getData().isMirrorSkin()
        );
        Npc ghost = FancyNpcsPlugin.get().getNpcAdapter().apply(ghostData);

        ghost.setSaveToFile(false);
        ghost.create();
        FancyNpcsPlugin.get().getNpcManager().registerNpc(ghost);
        ghost.removeForAll();

        ghost.spawn(player);
        return ghost;
    }

    @Override
    public void removeNpc(Npc npc) {
        if (npc == null) return;
        npc.removeForAll();

        FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
    }

    @Override
    public void updateNpcInstanceLocation(Npc npc, Location location) {
        if (npc == null) return;
        npc.getData().setLocation(location);

        UUID creatorId = npc.getData().getCreator();
        Player player = Bukkit.getPlayer(creatorId);

        if (player != null && player.isOnline()) {
            npc.move(player, false);
        } else {
            npc.removeForAll();
            FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
        }
    }
}
