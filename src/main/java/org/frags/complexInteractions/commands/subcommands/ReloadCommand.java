package org.frags.complexInteractions.commands.subcommands;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.SubCommand;

public class ReloadCommand extends SubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the configuration";
    }

    @Override
    public String getSyntax() {
        return "/interactions reload";
    }

    @Override
    public void perform(ComplexInteractions plugin, Player player, String[] args) {
        if (!player.hasPermission("interactions.reload") && !player.hasPermission("interactions.admin")) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return;
        }

        plugin.reloadConfig();
        player.sendMessage(ComplexInteractions.miniMessage.deserialize("Configuration reloaded"));
    }
}
