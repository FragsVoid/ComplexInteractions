package org.frags.complexInteractions.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.events.QuestCompleteEvent;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Conversation;

public class QuestCompleteListener implements Listener {

    private final ComplexInteractions plugin;

    public QuestCompleteListener(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent event) {
        Player player = event.getPlayer();
        Conversation conversation = plugin.getConversationManager().getConversation(event.getMissionId());
        if (conversation == null) {
            plugin.getLogger().severe("Invalid conversation. Player: " + player.getName());
            return;
        }
        Session session = new Session(plugin, player, conversation, plugin.getSessionManager());
        plugin.getSessionManager().addSession(player.getUniqueId(), session);
        session.startStage(conversation.getStage(conversation.getQuestCompleteConversation()));
    }
}
