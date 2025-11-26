package org.frags.complexInteractions.objects.conversation.matchers;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;

public class PDCMatcher implements ItemMatcher {

    private final NamespacedKey key;
    private final String requiredValue;

    public PDCMatcher(String namespace, String key, String requiredValue) {
        this.key = new NamespacedKey(namespace.toLowerCase(), key.toLowerCase());
        this.requiredValue = requiredValue;
    }

    @Override
    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) return false;

        if (requiredValue != null) {
            String foundValue = container.get(key, PersistentDataType.STRING);
            return requiredValue.equals(foundValue);
        }

        return true;
    }
}
