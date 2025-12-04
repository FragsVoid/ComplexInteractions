package org.frags.complexInteractions.managers;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Action;
import org.frags.complexInteractions.objects.conversation.Conversation;

public class ConversationScanner extends BukkitRunnable {

    private final SessionManager sessionManager;
    private final ConversationManager conversationManager;

    public ConversationScanner(SessionManager sessionManager, ConversationManager conversationManager) {
        this.sessionManager = sessionManager;
        this.conversationManager = conversationManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sessionManager.isConversing(player)) {
                checkEndRadius(player);
            } else {
                checkStartRadius(player);
            }
        }
    }

    private void checkEndRadius(Player player) {
        Session session = sessionManager.getSession(player);
        Conversation conversation = session.getConversation();

        long maxDist = conversation.getEndConversationRadius();
        if (maxDist <= 0) return;

        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(conversation.getNpcId());
        if (npc == null) return;

        Location loc = npc.getData().getLocation();

        if (player.getLocation().distanceSquared(loc) > (maxDist * maxDist)) {
            for (Action action : conversation.getInterruptActions()) {
                action.execute(player);
            }
        }
    }

    private void checkStartRadius(Player player) {

        for (Conversation conversation : conversationManager.getAllConversations()) {
            long startDist = conversation.getStarConversationRadius();
            if  (startDist <= 0) return;

            Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(conversation.getNpcId());
            if (npc == null) return;

            Location loc = npc.getData().getLocation();

            if (player.getLocation().distanceSquared(loc) > (startDist *startDist)) {
                sessionManager.startSession(player, conversation.getId());
                return;
            }
        }
    }
}
