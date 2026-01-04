package org.frags.complexInteractions.objects.conversation.actions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.Action;

public class RemoveItemAction extends Action {

    private final ItemStack item;
    private final int amount;

    private final String itemId;

    //[removeitem]preset:cabeza2 100
    //[removeitem]DIAMOND 10

    public RemoveItemAction(ItemStack item, int amount, String itemId) {
        this.item = item;
        this.amount = amount;
        this.itemId = itemId;
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

    @Override
    public String toString() {
        String str = (itemId == null) ? item.getType().name() : "preset:" + itemId;

        return "[removeitem]" + str + " " + amount;
    }
}
