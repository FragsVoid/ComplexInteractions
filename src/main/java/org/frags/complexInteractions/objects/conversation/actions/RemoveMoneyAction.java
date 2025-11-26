package org.frags.complexInteractions.objects.conversation.actions;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Action;

public class RemoveMoneyAction extends Action {

    private final double price;
    private final ComplexInteractions plugin;
    private final Economy economy;

    public RemoveMoneyAction(ComplexInteractions plugin, double price) {
        this.plugin = plugin;
        this.price = price;
        this.economy = ComplexInteractions.getEconomy();
    }

    @Override
    public boolean execute(Player player) {
        if (economy.getBalance(player) < price) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_money")));
            return false;
        }

        economy.withdrawPlayer(player, price);
        return true;
    }
}
