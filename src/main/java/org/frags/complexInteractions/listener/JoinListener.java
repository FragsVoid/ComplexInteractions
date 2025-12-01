package org.frags.complexInteractions.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frags.complexInteractions.ComplexInteractions;

public class JoinListener implements Listener {

    private ComplexInteractions plugin;

    public JoinListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getSessionManager().addPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getSessionManager().removePlayer(player);
    }
}
