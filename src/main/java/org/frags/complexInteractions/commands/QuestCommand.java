package org.frags.complexInteractions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.menu.QuestMenu;
import org.frags.customItems.CustomItems;
import org.jetbrains.annotations.NotNull;

public class QuestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) return true;

        new QuestMenu(ComplexInteractions.getInstance(), CustomItems.getInstance().getMenuManager().getPlayerMenuUtility(player)).open();
        return true;
    }
}
