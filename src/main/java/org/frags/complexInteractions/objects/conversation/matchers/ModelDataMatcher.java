package org.frags.complexInteractions.objects.conversation.matchers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class ModelDataMatcher implements ItemMatcher {

    private final int modelId;

    public ModelDataMatcher(int modelId) {
        this.modelId = modelId;
    }

    @Override
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return false;

        return item.getItemMeta().getCustomModelData() == modelId;
    }
}
