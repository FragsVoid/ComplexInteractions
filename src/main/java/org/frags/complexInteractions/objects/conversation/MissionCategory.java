package org.frags.complexInteractions.objects.conversation;

public enum MissionCategory {

    QUEST,
    MINI_QUEST;


    public static MissionCategory getMissionCategory(String missionCategory) {
        if (missionCategory == null) return null;
        return switch (missionCategory.toLowerCase()) {
            case "quest", "main" -> MissionCategory.QUEST;
            case "mini-quest", "mini", "mini_quest" -> MissionCategory.MINI_QUEST;
            default -> null;
        };
    }
}
