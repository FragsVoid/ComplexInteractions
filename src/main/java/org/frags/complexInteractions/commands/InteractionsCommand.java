package org.frags.complexInteractions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.subcommands.ReloadCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InteractionsCommand implements CommandExecutor {

    private ComplexInteractions plugin;

    private List<SubCommand> subCommands = new ArrayList<>();

    public InteractionsCommand(ComplexInteractions plugin) {
        this.plugin = plugin;
        subCommands.add(new ReloadCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player))
            return false;
        if (args.length > 0) {
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    subCommand.perform(plugin, player, args);
                }
            }
        } else if (args.length == 0) {
            player.sendMessage("--------------------------------");
            for (SubCommand subCommand : subCommands) {
                player.sendMessage(subCommand.getSyntax() + " - " + subCommand.getDescription());
            }
            player.sendMessage("--------------------------------");

        }

        return true;
    }

}
