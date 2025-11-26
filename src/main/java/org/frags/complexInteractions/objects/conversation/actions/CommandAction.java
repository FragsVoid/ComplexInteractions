package org.frags.complexInteractions.objects.conversation.actions;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.Action;

public class CommandAction extends Action {

    private final String command;
    private final boolean asConsole;

    public CommandAction(String command, boolean asConsole) {
        this.command = command;
        this.asConsole = asConsole;
    }

    @Override
    public boolean execute(Player player) {
        String finalCmd = command.replace("%player%", player.getName());

        finalCmd = PlaceholderAPI.setPlaceholders(player, finalCmd);

        if (asConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        } else {
            player.performCommand(finalCmd);
        }

        return true;
    }
}
