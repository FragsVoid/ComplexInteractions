package org.frags.complexInteractions.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.frags.complexInteractions.ComplexInteractions;

public class ChangeWorldListener implements Listener {

    private final ComplexInteractions plugin;

    public ChangeWorldListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        plugin.removePlayer(event.getFrom().getName(), player);

        plugin.addPlayer(player.getWorld().getName(), player);
    }
}
