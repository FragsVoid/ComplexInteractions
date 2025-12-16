package org.frags.complexInteractions.objects.conversation.requirements;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.Requirement;

public class PermissionRequirement extends Requirement {

    private final String permissionNode;

    public PermissionRequirement(String failMessage, String permissionNode) {
        super(failMessage);
        this.permissionNode = permissionNode;
    }

    @Override
    public boolean check(Player player) {
        return player.hasPermission(permissionNode);
    }

    @Override
    public String toString() {
        return "has_permission:" + permissionNode;
    }
}
