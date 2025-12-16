package org.frags.complexInteractions.objects.conversation.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class NormalMatcher implements ItemMatcher {

    private final ItemStack itemStack;
    private final String itemId;
    private final int amount;

    public NormalMatcher(ItemStack item, String itemId, int amount) {
        this.itemStack = item;
        this.itemId = itemId;
        this.amount = amount;
    }

    @Override
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        return item.isSimilar(this.itemStack);
    }

    @Override
    public String toString() {
        return "preset:" + itemId + " " + amount;
    }
}
