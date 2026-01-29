package org.frags.complexInteractions.objects;

import org.frags.complexInteractions.objects.missions.ActiveQuest;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DataStorage {

    void init();
    void close();

    void setCooldown(UUID player, String npcId, long cooldownTime);

    long getCooldown(UUID player, String npcId);

    void removeCooldown(UUID player, String npcId);

    Map<String, Long> getCooldowns(UUID player);

    void addCompletedConversation(UUID player, String conversationId);

    void removeCompletedConversation(UUID player, String conversationId);

    Set<String> getCompletedConversations(UUID player);

    Map<String, ActiveQuest> loadActiveQuests(UUID player);

    void saveActiveQuest(UUID player, ActiveQuest quest);

    void deleteActiveQuest(UUID player, String questId);
}
