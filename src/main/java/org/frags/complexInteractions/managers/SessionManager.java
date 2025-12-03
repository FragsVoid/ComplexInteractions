package org.frags.complexInteractions.managers;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private final ConversationManager conversationManager;

    private final Map<UUID, Session> activeSessions = new HashMap<>();
    private final ComplexInteractions plugin;

    public SessionManager(ConversationManager conversationManager, ComplexInteractions plugin) {
        this.conversationManager = conversationManager;
        this.plugin = plugin;
    }


    public void startSession(Player player, String conversationId) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("already_in_conversation")));
            return;
        }


        Conversation conversation = conversationManager.getConversation(conversationId);
        if (conversation == null) return;

        Session session = new Session(player, conversation, this);

        if (!conversation.canStart(player)) {
            ConversationStage noReqConversation = conversation.getConversationStageMap().get(conversation.getNoReqStageId());
            if (noReqConversation == null) return;

            session.startStage(noReqConversation);
            return;
        }

        activeSessions.put(player.getUniqueId(), session);
        session.startStage(conversation.getConversationStageMap().get(conversation.getStartStageId()));
    }

    public void endSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }

    public Session getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public boolean isConversing(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
}
