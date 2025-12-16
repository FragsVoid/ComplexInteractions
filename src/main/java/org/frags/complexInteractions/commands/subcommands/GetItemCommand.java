package org.frags.complexInteractions.commands.subcommands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.SubCommand;

public class GetItemCommand extends SubCommand {

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getDescription() {
        return "gets an item by an id";
    }

    @Override
    public String getSyntax() {
        return "/interactions get <id>";
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission("interactions.get") && player.hasPermission("interactions.admin");
    }

    @Override
    public void perform(ComplexInteractions plugin, Player player, String[] args) {
        if (!hasPermission(player)) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length != 2) {
            player.sendMessage("Wrong usage: " + getSyntax());
            return;
        }

        String itemId = args[1].toLowerCase();
        ItemStack item = plugin.getItemManager().getItem(itemId);
        if (item == null) {
            player.sendMessage("That itemn is null.");
            return;
        }

        player.getInventory().addItem(item);
        player.sendMessage("Item added!");
    }
}
