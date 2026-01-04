package org.frags.complexInteractions.objects.conversation;

public enum MissionFilter {
    ALL("Todas"),
    MAIN_QUEST("Principales"),
    SIDE_QUEST("Secundarias"),
    AVAILABLE("Disponibles"),
    COOLDOWN("En Espera");

    private final String displayName;

    MissionFilter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MissionFilter next() {
        int nextIndex = (this.ordinal() + 1) % values().length;
        return values()[nextIndex];
    }
}