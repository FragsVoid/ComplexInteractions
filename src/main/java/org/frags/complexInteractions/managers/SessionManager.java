package org.frags.complexInteractions.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.events.MissionCompleteEvent;
import org.frags.complexInteractions.objects.Session;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;
import org.frags.complexInteractions.objects.missions.ActiveQuest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final ConversationManager conversationManager;

    private final Map<UUID, Session> activeSessions = new HashMap<>();
    private final ComplexInteractions plugin;

    private final Map<UUID, Set<String>> completedConversations = new ConcurrentHashMap<>();

    public SessionManager(ConversationManager conversationManager, ComplexInteractions plugin) {
        this.conversationManager = conversationManager;
        this.plugin = plugin;
    }

    public void loadCompletedConversations(UUID uuid) {
        completedConversations.remove(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           Set<String> completed = plugin.getDatabase().getCompletedConversations(uuid);

           Bukkit.getScheduler().runTask(plugin, () -> {
               if (completed != null && !completed.isEmpty()) {
                   completedConversations.put(uuid, completed);
               }
           });
        });
    }

    public void unloadPlayer(UUID uuid) {
        completedConversations.remove(uuid);
        activeSessions.remove(uuid);
    }

    public boolean addSession(UUID uuid, Session session) {
        if (activeSessions.containsKey(uuid)) {
            return false;
        }
        activeSessions.put(uuid, session);
        return true;
    }

    public void removeConversation(UUID uuid, String conversationId) {
        Conversation conversation = plugin.getConversationManager().getConversation(conversationId);
        String id;
        if (conversation == null) {
            id = conversationId;
        } else {
            id = conversation.getId();
        }

        if (completedConversations.containsKey(uuid)) {
            completedConversations.get(uuid).remove(id);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabase().removeCompletedConversation(uuid, id);
        });
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabase().addCompletedConversation(uuid, conversation);
        });
    }

    public void startSession(Player player, String conversationId) {

        Conversation conversation = conversationManager.getConversation(conversationId);
        if (conversation == null) return;

        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("already_in_conversation")));
            return;
        }

        Map<String, ActiveQuest> activeQuests = plugin.getQuestManager().getQuests(player);
        String questId = conversation.getQuestId();
        if (activeQuests.containsKey(questId)) {
            ActiveQuest quest = activeQuests.get(questId);
            ConversationStage conversationStage;
            if (quest.isComplete()) {
                String rewardStageId = conversation.getQuestCompletedConversation();
                conversationStage = conversation.getStage(rewardStageId);
            } else {
                conversationStage = conversation.getStage(conversation.getQuestInProgress());
            }
            if (conversationStage != null) {
                Session session = new Session(plugin, player, conversation, this);
                activeSessions.put(player.getUniqueId(), session);
                session.startStage(conversationStage);
                session.setTurningInQuest(true);
                return;
            }
        }

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), conversationId)) {
            ConversationStage cooldownStage = conversation.getConversationStageMap().get(conversation.getCooldownMessage());
            if (cooldownStage == null) return;
            Session session = new Session(plugin, player, conversation, this);
            session.startStage(cooldownStage);
            return;
        }

        Session session = new Session(plugin, player, conversation, this);
        if (conversation.isOnlyOnce() && hasCompleted(player.getUniqueId(), conversation.getId())) {
            ConversationStage alreadyCompletedStage = conversation.getConversationStageMap().get(conversation.getAlreadyCompletedStageId());
            if (alreadyCompletedStage == null) return;

            session.startStage(alreadyCompletedStage);
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
            Conversation conversation = session.getConversation();
            session.cleanup();
            if (completed) {
                ConversationStage stage = session.getStage();
                if (!stage.getCompletesConversation()) return;

                String questId = conversation.getQuestId();

                Map<String, ActiveQuest> activeQuests = plugin.getQuestManager().getQuests(player);
                if (questId != null && activeQuests.containsKey(questId)) {
                    ActiveQuest quest = activeQuests.get(questId);

                    if (quest.isComplete()) {

                        plugin.getQuestManager().removeQuest(player, questId);

                        completedConversation(player.getUniqueId(), conversation.getId());

                        plugin.getServer().getPluginManager().callEvent(new MissionCompleteEvent(player, conversation.getId()));

                        return;
                    }
                }
                if (conversation.isOnlyOnce()) {
                    completedConversation(player.getUniqueId(), session.getConversation().getId());
                    plugin.getServer().getPluginManager().callEvent(new MissionCompleteEvent(player, session.getConversation().getId()));
                    return;
                }
                plugin.getServer().getPluginManager().callEvent(new MissionCompleteEvent(player, session.getConversation().getId()));
                long cooldownTime = session.getConversation().getCooldown();
                if (cooldownTime > 0) {
                    long expiry = System.currentTimeMillis() + (cooldownTime * 1000);

                    plugin.getCooldownManager().setCooldown(player.getUniqueId(), session.getConversation().getNpcId(), cooldownTime);

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        plugin.getDatabase().setCooldown(player.getUniqueId(), session.getConversation().getNpcId(), expiry);
                    });
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
