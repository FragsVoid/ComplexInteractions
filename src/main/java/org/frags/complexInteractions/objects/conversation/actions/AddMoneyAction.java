package org.frags.complexInteractions.objects.conversation.actions;

import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Action;

public class AddMoneyAction extends Action {

    private final double price;

    public AddMoneyAction(double price) {
        this.price = price;
    }

    @Override
    public boolean execute(Player player) {
        ComplexInteractions.getEconomy().depositPlayer(player, price);
        return true;
    }

    @Override
    public String toString() {
        return "[addmoney]" + price;
    }
}
