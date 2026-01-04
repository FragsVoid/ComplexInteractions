package org.frags.complexInteractions.managers;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.events.MissionCompleteEvent;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;

import java.util.*;

public class SessionManager {

    private final ConversationManager conversationManager;

    private final Map<UUID, Session> activeSessions = new HashMap<>();
    private final ComplexInteractions plugin;

    private final Map<UUID, Set<String>> completedConversations = new HashMap<>();

    public SessionManager(ConversationManager conversationManager, ComplexInteractions plugin) {
        this.conversationManager = conversationManager;
        this.plugin = plugin;
    }

    public void loadCompletedConversations(UUID uuid) {
        completedConversations.remove(uuid);

        List<String> conversationsId = plugin.getDataFile().getConfig().getStringList("completed." + uuid.toString());
        if (conversationsId.isEmpty()) return;

        Set<String> conversations = new HashSet<>(conversationsId);

        completedConversations.put(uuid, conversations);
    }

    public void saveAllConversations() {
        for (Map.Entry<UUID, Set<String>> entry : completedConversations.entrySet()) {
            Set<String> conversations = entry.getValue();
            if (conversations == null || conversations.isEmpty()) {
                plugin.getDataFile().getConfig().set("completed." + entry.getKey().toString(), null);
                continue;
            }


            plugin.getDataFile().getConfig().set("completed." + entry.getKey().toString(), new ArrayList<>(conversations));
        }
        plugin.getDataFile().saveConfig();
    }

    public void removeConversation(UUID uuid, String conversationId) {
        Conversation conversation = plugin.getConversationManager().getConversation(conversationId);
        String id;
        if (conversation == null) {
            id = conversationId;
        } else {
            id = conversation.getId();
        }

        completedConversations.computeIfAbsent(uuid, k -> new HashSet<>()).remove(id);
    }

    public boolean hasCompleted(UUID uuid, Conversation conversation) {
        return hasCompleted(uuid, conversation.getId());
    }

    public boolean hasCompleted(UUID uuid, String conversation) {
        Set<String> conversations = completedConversations.get(uuid);
        if (conversations == null || conversations.isEmpty()) return false;
        return conversations.contains(conversation);
    }

    public void completedConversation(UUID uuid, Conversation conversation) {
        completedConversation(uuid, conversation.getId());
    }

    public void completedConversation(UUID uuid, String conversation) {
        completedConversations.computeIfAbsent(uuid, k -> new HashSet<>()).add(conversation);
    }

    public void startSession(Player player, String conversationId) {

        Conversation conversation = conversationManager.getConversation(conversationId);
        if (conversation == null) return;
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("already_in_conversation")));
            return;
        }

        Session session = new Session(plugin, player, conversation, this);

        if (conversation.isOnlyOnce() && hasCompleted(player.getUniqueId(), conversation.getId())) {
            ConversationStage alreadyCompletedStage = conversation.getConversationStageMap().get(conversation.getAlreadyCompletedStageId());
            if (alreadyCompletedStage == null) return;

            session.startStage(alreadyCompletedStage);
            return;
        }

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), conversationId)) {
            ConversationStage cooldownStage = conversation.getConversationStageMap().get(conversation.getCooldownMessage());
            if (cooldownStage == null) return;

            session.startStage(cooldownStage);
            return;
        }

        if (!conversation.canStart(player)) {
            ConversationStage noReqConversation = conversation.getConversationStageMap().get(conversation.getNoReqStageId());
            if (noReqConversation == null) return;

            session.startStage(noReqConversation);
            return;
        }



        activeSessions.put(player.getUniqueId(), session);
        session.start();
    }

    public String getRemainingTimeFormatted(long cooldownSeconds) {
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
                ConversationStage stage = session.getStage();
                if (!stage.getCompletesConversation()) return;
                if (session.getConversation().isOnlyOnce()) {
                    completedConversation(player.getUniqueId(), session.getConversation().getId());
                    return;
                }
                plugin.getServer().getPluginManager().callEvent(new MissionCompleteEvent(player, session.getConversation().getId()));
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
