package org.frags.complexInteractions.objects.conversation.actions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.Action;

public class RemoveItemAction extends Action {

    private final ItemStack item;
    private final int amount;

    public RemoveItemAction(ItemStack item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    @Override
    public boolean execute(Player player) {

        int count = amount;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (count <= 0) break;
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            if (!item.isSimilar(itemStack)) continue;

            int currentAmount = itemStack.getAmount();
            int toRemove = Math.min(currentAmount, count);

            itemStack.setAmount(currentAmount - toRemove);
            count -= toRemove;
        }

        return true;
    }
}
