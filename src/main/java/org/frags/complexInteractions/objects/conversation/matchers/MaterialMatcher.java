package org.frags.complexInteractions.objects.conversation.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class MaterialMatcher implements ItemMatcher {

    private final Material material;

    public MaterialMatcher(Material material) {
        this.material = material;
    }


    @Override
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!isItemDefault(item)) return false;

        return material == item.getType();
    }

    private boolean isItemDefault(ItemStack item) {
        if (!item.hasItemMeta()) return true;

        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName()) return false;
        if (meta.hasLore()) return false;
        if (meta.hasEnchants()) return false;
        if (!meta.getPersistentDataContainer().isEmpty()) return false;

        return true;
    }
}
