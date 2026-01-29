package org.frags.complexInteractions.listener;

import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.frags.complexInteractions.managers.ConversationManager;
import org.frags.complexInteractions.managers.SessionManager;
import org.frags.complexInteractions.objects.conversation.Conversation;

public class NpcInteractionListener implements Listener {

    private final ConversationManager conversationManager;
    private final SessionManager sessionManager;

    public NpcInteractionListener(ConversationManager conversationManager, SessionManager sessionManager) {
        this.conversationManager = conversationManager;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onNpcInteract(NpcInteractEvent event) {
        String nameId = event.getNpc().getData().getName();
        Conversation conversation = conversationManager.getConversation(nameId);
        if (conversation == null) return;

        sessionManager.startSession(event.getPlayer(), nameId);

    }
}
