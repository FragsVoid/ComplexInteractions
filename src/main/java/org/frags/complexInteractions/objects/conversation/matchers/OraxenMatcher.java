package org.frags.complexInteractions.objects.conversation.matchers;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class OraxenMatcher implements ItemMatcher {

    private final String oraxenId;

    public OraxenMatcher(String oraxenId) {
        this.oraxenId = oraxenId;
    }

    @Override
    public boolean matches(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String id = OraxenItems.getIdByItem(item);
        return oraxenId.equals(id);
    }
}
