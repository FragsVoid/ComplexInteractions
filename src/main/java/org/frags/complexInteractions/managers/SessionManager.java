package org.frags.complexInteractions.managers;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private ComplexInteractions plugin;

    private Map<UUID, Session> sessions;

    public SessionManager(ComplexInteractions plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }


    public void addPlayer(Player player) {
        sessions.put(player.getUniqueId(), new Session(player, null));
    }

    public void removePlayer(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public Session getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

}
