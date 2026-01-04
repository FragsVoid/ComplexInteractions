package org.frags.complexInteractions.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frags.complexInteractions.ComplexInteractions;

public class QuitListener implements Listener {

    private ComplexInteractions plugin;

    public QuitListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getSessionManager().loadCompletedConversations(event.getPlayer().getUniqueId());
        Player player = event.getPlayer();
        plugin.addPlayer(player.getWorld().getName(), player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSessionManager().endSession(event.getPlayer(), false);
        Player player = event.getPlayer();
        plugin.removePlayer(player.getWorld().getName(), player);
    }
}
