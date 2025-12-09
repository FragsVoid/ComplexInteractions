package org.frags.complexInteractions.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.SubCommand;

public class ResetCooldownCommand extends SubCommand {

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getSyntax() {
        return "/interactions reset <player> <id>";
    }

    @Override
    public void perform(ComplexInteractions plugin, Player player, String[] args) {
        if (!player.hasPermission("interactions.reset") && !player.hasPermission("interactions.admin")) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length != 3) {
            player.sendMessage("Wrong usage: /interactions reset <player> <id>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore()) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize("That is not a valid name!"));
            return;
        }

        plugin.getCooldownManager().resetCooldown(target.getUniqueId(), args[2]);
        player.sendMessage(ComplexInteractions.miniMessage.deserialize("<green>Reseted"));
    }
}
