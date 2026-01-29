package org.frags.complexInteractions.objects.conversation.requirements;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Requirement;

import java.util.List;

public class CompletedRequirement extends Requirement {

    private final ComplexInteractions plugin;
    private final List<String> conversations;

    public CompletedRequirement(String failMessage, List<String> conversations, ComplexInteractions plugin) {
        super(failMessage);
        this.conversations = conversations;
        this.plugin = plugin;
    }

    @Override
    public boolean check(Player player) {
        for (String conversation : conversations) {
            if (!plugin.getSessionManager().hasCompleted(player.getUniqueId(), conversation)) return false;
        }
        return true;
    }
}
