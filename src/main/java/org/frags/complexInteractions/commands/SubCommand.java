package org.frags.complexInteractions.commands;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;

public abstract class SubCommand {

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getSyntax();

    public abstract boolean hasPermission(Player player);

    public abstract void perform(ComplexInteractions plugin, Player player, String[] args);
}
