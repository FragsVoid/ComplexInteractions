package org.frags.complexInteractions.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemProvider;

import java.util.*;
import java.util.stream.Collectors;

public class ItemManager implements ItemProvider {

    private final ComplexInteractions plugin;
    private final Map<String, ItemStack> itemMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();


    public ItemManager(ComplexInteractions plugin) {
        this.plugin = plugin;
        loadItems();
    }

    @Override
    public ItemStack getItem(String id) {
        ItemStack item = itemMap.get(id);
        if (item == null) return null;
        return item.clone();
    }

    public Set<String> getAllIds() {
        return itemMap.keySet();
    }

    public void loadItems() {
        itemMap.clear();

        ConfigurationSection itemsSection = plugin.getItemsFile().getConfig().getConfigurationSection("items");

        if (itemsSection == null) return;

        int count = 0;

        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(itemId);
            if (section == null) continue;

            if (section.contains("bytes")) {
                String serializedBytes = section.getString("bytes");
                byte[] serializedItem =  Base64.getDecoder().decode(serializedBytes);
                ItemStack item = ItemStack.deserializeBytes(serializedItem);

                if (item.getType() != Material.AIR) {
                    itemMap.put(itemId, item);
                    count++;
                }

                continue;
            }

            String materialName = section.getString("material");
            Material material = Material.matchMaterial(materialName);

            if (material == null) {
                plugin.getLogger().warning("Material not found: " + materialName);
                continue;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (section.contains("name")) {
                Component name = transformString(section.getString("name"));
                meta.displayName(name);
            }

            if (section.contains("lore")) {
                List<Component> newLore = new ArrayList<>();
                for (String line : section.getStringList("lore")) {
                    newLore.add(transformString(line));
                }
                meta.lore(newLore);
            }

            if (section.contains("custom_model_data")) {
                meta.setCustomModelData(section.getInt("custom_model_data"));
            }

            List<String> flags = section.getStringList("flags");
            for (String flagName : flags) {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            ConfigurationSection pdcSection = section.getConfigurationSection("pdc");
            if (pdcSection != null) {
                for (String keyRaw : pdcSection.getKeys(false)) {
                    String value = pdcSection.getString(keyRaw);
                    // Formato esperado en config -> "namespace:key": "valor"
                    // Ejemplo: "mmoitems:id": "SWORD"
                    String[] keyParts = keyRaw.split(":");
                    NamespacedKey key;
                    if (keyParts.length == 2) {
                        key = new NamespacedKey(keyParts[0], keyParts[1]);
                    } else {
                        key = new NamespacedKey(plugin, keyRaw);
                    }

                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
                }
            }

            if (section.contains("base64") && material == Material.PLAYER_HEAD) {
                if (meta instanceof SkullMeta skullMeta) {
                    String base64 = section.getString("base64");

                    String uuidString = section.getString("uuid");
                    UUID id;
                    if (uuidString != null) {
                        try {
                            id = UUID.fromString(uuidString);
                        } catch (IllegalArgumentException e) {
                            id = UUID.nameUUIDFromBytes(base64.getBytes());
                        }
                    } else {
                        id = UUID.nameUUIDFromBytes(base64.getBytes());
                    }

                    applyTexture(skullMeta, base64, id);
                }
            }

            if (section.contains("skull_owner") && material == Material.PLAYER_HEAD) {
                String ownerName =  section.getString("skull_owner");
                if (meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName));
                }
            }

            item.setItemMeta(meta);
            itemMap.put(itemId, item);
            count++;
        }
    }

    private Component transformString(String string) {
        return mm.deserialize(string).decoration(TextDecoration.ITALIC, false);
    }

    private void applyTexture(SkullMeta meta, String base64, UUID id) {
        if (base64 == null || base64.isEmpty()) return;

        PlayerProfile profile = Bukkit.createProfile(id);

        profile.setProperty(new ProfileProperty("textures", base64));

        meta.setPlayerProfile(profile);
    }
}
