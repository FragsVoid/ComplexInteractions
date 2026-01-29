package org.frags.complexInteractions.objects.conversation.actions;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.npcmovermanager.NpcAIMover;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Action;
import org.frags.complexInteractions.objects.conversation.ConversationStage;
import org.frags.complexInteractions.objects.conversation.interfaces.NpcAdapter;

import java.util.List;

public class WalkAction extends Action {

    private final ComplexInteractions plugin;
    private final String npcId;
    private final List<Location> path;
    private final String nextStageId;
    private final String nextNpc;

    public WalkAction(ComplexInteractions plugin, String npcId, List<Location> path, String nextStageId, String nextNpc) {
        this.plugin = plugin;
        this.npcId = npcId;
        this.path = path;
        this.nextStageId = nextStageId;
        this.nextNpc = nextNpc;
    }

    @Override
    public boolean execute(Player player) {
        NpcAIMover mover = plugin.getWalkingManager().getNpcAIMover();
        NpcAdapter adapter = plugin.getNpcAdapter();
        Session session = plugin.getSessionManager().getSession(player);

        if (session != null) {
            session.setWaitingForMovement(true);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            adapter.hideNpcForPlayer(npcId, player);

            Npc ghostNpc = adapter.createGhostNpc(npcId, player);

            if (ghostNpc == null) {
                adapter.showNpcForPlayer(npcId, player);
                if (session != null) session.setWaitingForMovement(false);
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {

                adapter.hideNpcForPlayer(npcId, player);

                mover.walkGhost(ghostNpc, npcId, path, () -> {

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        adapter.removeNpc(ghostNpc);

                        if (nextNpc != null && !nextNpc.equalsIgnoreCase("none") && !nextNpc.isEmpty()) {
                            adapter.showNpcForPlayer(nextNpc, player);
                        }

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (session != null && !session.isEnded()) {
                                session.setWaitingForMovement(false);

                                if (nextStageId != null) {
                                    ConversationStage nextStage = session.getConversation().getStage(nextStageId);
                                    if (nextStage != null) {
                                        session.startStage(nextStage);
                                    } else {
                                        plugin.getSessionManager().endSession(player, true);
                                    }
                                } else {
                                    plugin.getSessionManager().endSession(player, true);
                                }
                            }
                        });
                    });
                });
            });
        });

        return true;
    }
}