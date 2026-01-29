package org.frags.complexInteractions.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frags.complexInteractions.ComplexInteractions;

import java.util.UUID;

public class QuitListener implements Listener {

    private ComplexInteractions plugin;

    public QuitListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        plugin.getSessionManager().loadCompletedConversations(event.getPlayer().getUniqueId());

        plugin.getCooldownManager().loadPlayerCooldowns(uuid);

        plugin.getQuestManager().loadPlayerData(uuid);

        Player player = event.getPlayer();
        plugin.addPlayer(player.getWorld().getName(), player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getSessionManager().endSession(player, false);
        plugin.getSessionManager().unloadPlayer(uuid);
        plugin.getCooldownManager().unloadPlayer(uuid);
        plugin.getQuestManager().savePlayerData(uuid);
        plugin.removePlayer(player.getWorld().getName(), player);

    }
}
