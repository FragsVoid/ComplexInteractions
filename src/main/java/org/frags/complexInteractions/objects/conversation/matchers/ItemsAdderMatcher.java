package org.frags.complexInteractions.objects.conversation.matchers;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class ItemsAdderMatcher implements ItemMatcher {

    private final String id;

    public ItemsAdderMatcher(String id) {
        this.id = id;
    }

    @Override
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null) return false;

        return stack.getId().equals(this.id);
    }
}
