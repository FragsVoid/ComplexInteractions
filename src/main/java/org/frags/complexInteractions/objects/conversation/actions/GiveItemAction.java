package org.frags.complexInteractions.objects.conversation.actions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Action;

import java.util.Map;

public class GiveItemAction extends Action {

    private final ItemStack item;
    private final int amount;

    private final String itemId;

    //[giveitem]preset:cabeza2 100
    //[giveitem]DIAMOND 10

    public GiveItemAction(ItemStack item, int amount, String itemId) {
        this.item = item;
        this.amount = amount;
        this.itemId = itemId;
    }

    @Override
    public boolean execute(Player player) {
        int remaining = amount;
        int maxStackSize = item.getMaxStackSize();
        boolean messagedFull = false;

        while (remaining > 0) {
            int currentAmount = Math.min(remaining, maxStackSize);
            ItemStack toAdd = item.clone();
            toAdd.setAmount(currentAmount);

            Map<Integer, ItemStack> leftOvers = player.getInventory().addItem(toAdd);

            if (!leftOvers.isEmpty()) {
                if (!messagedFull) {
                    player.sendMessage(ComplexInteractions.miniMessage.deserialize(
                            ComplexInteractions.getInstance().getMessage("full_inventory")
                    ));
                    messagedFull = true;
                }
                for (ItemStack drop : leftOvers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
            remaining -= currentAmount;
        }
        return true;
    }


    @Override
    public String toString() {
        String str = (itemId == null) ? item.getType().name() : "preset:" + itemId;

        return "[giveitem]" + str + " " + amount;
    }
}
