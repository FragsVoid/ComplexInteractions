package org.frags.complexInteractions.objects.conversation;

import org.bukkit.entity.Player;

import java.util.List;

public class Option {

    private String id;
    private String text;
    private String nextStage;

    private List<Action> onClickActions;
    private List<Requirement> requirements;

    public Option(String id, String text, String nextStage, List<Action> onClickActions, List<Requirement> requirements) {
        this.id = id;
        this.text = text;
        this.nextStage = nextStage;
        this.onClickActions = onClickActions;
        this.requirements = requirements;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getNextStage() {
        return nextStage;
    }

    public List<Action> getOnClickActions() {
        return onClickActions;
    }

    public boolean hasRequirements(Player player) {
        for (Requirement requirement : requirements) {
            if (!requirement.check(player)) return false;
        }

        return true;
    }
}
