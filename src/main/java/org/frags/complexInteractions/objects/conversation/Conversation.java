package org.frags.complexInteractions.objects.conversation;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Conversation {

    private String id;
    private String npcId;
    private boolean blockMovement;
    private boolean slowEffect;
    private long starConversationRadius;
    private long endConversationRadius;
    private String startStageId;
    private String noReqStageId;
    private String cooldownMessage;
    private String npcName;
    private String alreadyCompletedStageId;
    private boolean mission;
    private MissionCategory missionCategory;

    private Component missionName;
    private List<Component> missionLore;

    private Map<String, ConversationStage> conversationStageMap;
    private List<Action> interruptActions;

    private List<Requirement> requirements;

    private long cooldown;

    private boolean onlyOnce;

    private Material icon;

    public Conversation(String id, String npcId, boolean blockMovement, boolean slowEffect, long starConversationRadius,
                        long endConversationRadius, String startStageId, String noReqStageId, String npcName,
                        Map<String, ConversationStage> conversationStageMap, List<Action> interruptActions,
                        List<Requirement> requirements, long cooldown, String cooldownMessage, boolean onlyOnce, String alreadyCompletedStageId,
                        boolean isMission, MissionCategory missionCategory,  Component missionName, List<Component> missionLore,
                        Material icon) {
        this.id = id;
        this.npcId = npcId;
        this.blockMovement = blockMovement;
        this.slowEffect = slowEffect;
        this.starConversationRadius = starConversationRadius;
        this.endConversationRadius = endConversationRadius;
        this.startStageId = startStageId;
        this.noReqStageId = noReqStageId;
        this.npcName = npcName;
        this.conversationStageMap = conversationStageMap;
        this.interruptActions = interruptActions;
        this.requirements = requirements;
        this.cooldown = cooldown;
        this.cooldownMessage = cooldownMessage;
        this.onlyOnce = onlyOnce;
        this.alreadyCompletedStageId = alreadyCompletedStageId;
        this.mission = isMission;
        this.missionCategory = missionCategory;
        this.missionName = missionName;
        this.missionLore = missionLore;
        this.icon = icon;
    }

    public boolean canStart(Player player) {
        for (Requirement requirement : requirements) {
            if (!requirement.check(player)) return false;
        }

        return true;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean isMission() {
        return mission;
    }

    public MissionCategory getMissionCategory() {
        return missionCategory;
    }

    public Component getMissionName() {
        return missionName;
    }

    public List<Component> getMissionLore() {
        return missionLore;
    }

    public ConversationStage getStage(String stageId) {
        return conversationStageMap.get(stageId);
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }

    public boolean isOnlyOnce() {
        return onlyOnce;
    }

    public String getAlreadyCompletedStageId() {
        return alreadyCompletedStageId;
    }


    public String getId() {
        return id;
    }

    public String getNpcId() {
        return npcId;
    }

    public boolean isBlockMovement() {
        return blockMovement;
    }

    public boolean isSlowEffect() {
        return slowEffect;
    }

    public long getStarConversationRadius() {
        return starConversationRadius;
    }

    public long getEndConversationRadius() {
        return endConversationRadius;
    }

    public String getStartStageId() {
        return startStageId;
    }

    public String getNoReqStageId() {
        return noReqStageId;
    }

    public String getNpcName() {
        return npcName;
    }

    public Map<String, ConversationStage> getConversationStageMap() {
        return conversationStageMap;
    }

    public List<Action> getInterruptActions() {
        return interruptActions;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void addConversationStage(String stageId, ConversationStage conversationStage) {
        conversationStageMap.put(stageId, conversationStage);
    }

    public void addRequirement(Requirement requirement) {
        requirements.add(requirement);
    }

    public void addInterruptAction(Action action) {
        interruptActions.add(action);
    }

    public List<ConversationStage> getConversationStages() {
        return new ArrayList<>(conversationStageMap.values());
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }

    public void setBlockMovement(boolean blockMovement) {
        this.blockMovement = blockMovement;
    }

    public void setSlowEffect(boolean slowEffect) {
        this.slowEffect = slowEffect;
    }

    public void setStarConversationRadius(long starConversationRadius) {
        this.starConversationRadius = starConversationRadius;
    }

    public void setEndConversationRadius(long endConversationRadius) {
        this.endConversationRadius = endConversationRadius;
    }

    public void setStartStageId(String startStageId) {
        this.startStageId = startStageId;
    }

    public void setNoReqStageId(String noReqStageId) {
        this.noReqStageId = noReqStageId;
    }

    public void setCooldownMessage(String cooldownMessage) {
        this.cooldownMessage = cooldownMessage;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }

    public void setAlreadyCompletedStageId(String alreadyCompletedStageId) {
        this.alreadyCompletedStageId = alreadyCompletedStageId;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public void setOnlyOnce(boolean onlyOnce) {
        this.onlyOnce = onlyOnce;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Conversation conversation) {
            return conversation.getId().equals(this.id);
        }

        return false;
    }
}
