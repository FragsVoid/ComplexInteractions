package org.frags.complexInteractions.objects.conversation;

import org.bukkit.entity.Player;

public abstract class Requirement {

    private final String message;

    public Requirement(String failMessage) {
        this.message = failMessage;
    }

    public abstract boolean check(Player player);

    public String getFailMessage() {
        return message;
    }
}
