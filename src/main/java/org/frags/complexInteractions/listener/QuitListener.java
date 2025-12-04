package org.frags.complexInteractions.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frags.complexInteractions.ComplexInteractions;

public class QuitListener implements Listener {

    private ComplexInteractions plugin;

    public QuitListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSessionManager().endSession(event.getPlayer(), false);
    }
}
