package org.frags.complexInteractions.objects;

import java.util.List;
import java.util.Map;

public class Conversation {

    private String id;
    private String npcId;
    private boolean blockMovement;
    private boolean slowEffect;
    private long starConversationRadius;
    private long endConversationRadius;
    private String startStageId;
    private String noReqStageId;
    private String npcName;

    private Map<String, ConversationStage> conversationStageMap;
    private List<String> interruptActions;

    private List<String> requirements;

    private long cooldown;

    public Conversation(String id, String npcId, boolean blockMovement, boolean slowEffect, long starConversationRadius,
                        long endConversationRadius, String startStageId, String noReqStageId, String npcName,
                        Map<String, ConversationStage> conversationStageMap, List<String> interruptActions,
                        List<String> requirements, long cooldown) {
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

    public List<String> getInterruptActions() {
        return interruptActions;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public long getCooldown() {
        return cooldown;
    }
}
