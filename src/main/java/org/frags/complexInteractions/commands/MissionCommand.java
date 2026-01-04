package org.frags.complexInteractions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.menu.MissionMenu;
import org.frags.complexInteractions.menu.MissionMenuUtility;
import org.frags.customItems.CustomItems;
import org.frags.customItems.menu.PlayerMenuUtility;
import org.jetbrains.annotations.NotNull;

public class MissionCommand implements CommandExecutor {

    private final ComplexInteractions plugin;

    public MissionCommand(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        PlayerMenuUtility playerMenuUtility = CustomItems.getInstance().getMenuManager().getPlayerMenuUtility((Player) sender);
        if (!(playerMenuUtility instanceof MissionMenuUtility)) {
            playerMenuUtility = new MissionMenuUtility((Player) sender);
        }

        new MissionMenu(plugin, (MissionMenuUtility) playerMenuUtility, 0).open();
        return true;
    }
}
