package org.frags.complexInteractions.managers;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    private final ConversationManager conversationManager;

    private final Map<UUID, Session> activeSessions = new HashMap<>();
    private final ComplexInteractions plugin;

    public SessionManager(ConversationManager conversationManager, ComplexInteractions plugin) {
        this.conversationManager = conversationManager;
        this.plugin = plugin;
    }


    public void startSession(Player player, String conversationId) {

        Conversation conversation = conversationManager.getConversation(conversationId);
        if (conversation == null) return;
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("already_in_conversation")));
            return;
        }

        Session session = new Session(player, conversation, this);

        if (!conversation.canStart(player)) {
            ConversationStage noReqConversation = conversation.getConversationStageMap().get(conversation.getNoReqStageId());
            if (noReqConversation == null) return;

            session.startStage(noReqConversation);
            return;
        }

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), conversationId)) {
            long left = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), conversationId);
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(conversation.getCooldownMessage().replace("%time%", getRemainingTimeFormatted(left))));
            return;
        }

        activeSessions.put(player.getUniqueId(), session);
        session.start();
    }

    private String getRemainingTimeFormatted(long cooldownSeconds) {
        long hours = cooldownSeconds / 3600;
        long minutes =  (cooldownSeconds % 3600) / 60;
        long seconds =  cooldownSeconds % 60;

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

    public void endSession(Player player, boolean completed) {
        Session session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.cleanup();
            if (completed) {
                long cooldownTime = session.getConversation().getCooldown();
                if (cooldownTime > 0) {
                    plugin.getCooldownManager().setCooldown(player.getUniqueId(), session.getConversation().getNpcId(), cooldownTime);
                }
            }
        }
    }

    public Session getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public boolean isConversing(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
}
