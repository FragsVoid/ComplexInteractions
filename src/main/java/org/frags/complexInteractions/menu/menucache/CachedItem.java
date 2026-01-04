package org.frags.complexInteractions.menu.menucache;

import org.bukkit.inventory.ItemStack;

public class CachedItem {
    private final ItemStack itemStack;
    private final int slot;

    public CachedItem(ItemStack itemStack, int slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public ItemStack getItemStack() {
        return itemStack.clone(); // Clone por seguridad
    }

    public int getSlot() {
        return slot;
    }
}