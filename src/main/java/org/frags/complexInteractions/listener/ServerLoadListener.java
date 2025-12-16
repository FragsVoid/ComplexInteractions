package org.frags.complexInteractions.listener;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.frags.complexInteractions.managers.WalkingManager;
import org.frags.complexInteractions.objects.walking.WalkingObject;

public class ServerLoadListener implements Listener {

    private WalkingManager walkingManager;

    public ServerLoadListener(WalkingManager walkingManager) {
        this.walkingManager = walkingManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        for (Npc npc : FancyNpcsPlugin.get().getNpcManager().getAllNpcs()) {
            String id = npc.getData().getName();
            WalkingObject walkingObject = walkingManager.getWalkingByNpcId(id);
            if (walkingObject == null) {
                System.out.println(id + " Is null");
                continue;
            }
            npc.getData().setLocation(walkingObject.getWaypoint(walkingObject.getStartWaypoint()).getLocation());
        }
    }
}
