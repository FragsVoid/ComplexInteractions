package org.frags.complexInteractions.objects.conversation.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class NormalMatcher implements ItemMatcher {

    private final ItemStack itemStack;

    public NormalMatcher(ItemStack item) {
        this.itemStack = item;
    }

    @Override
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        return item.isSimilar(this.itemStack);
    }
}
