package org.frags.complexInteractions.commands.subcommands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.commands.SubCommand;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class ImportCommand extends SubCommand {

    @Override
    public String getName() {
        return "import";
    }

    @Override
    public String getDescription() {
        return "Imports the item in your hand";
    }

    @Override
    public String getSyntax() {
        return "/interactions import <key>";
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.hasPermission("interactions.import") && player.hasPermission("interactions.admin");
    }

    @Override
    public void perform(ComplexInteractions plugin, Player player, String[] args) {
        if (!player.hasPermission("interactions.import") && !player.hasPermission("interactions.admin")) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return;
        }

        if (args.length != 2) {
            player.sendMessage("Wrong usage: " + getSyntax());
            return;
        }

        String itemId = args[1].toLowerCase();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            player.sendMessage("You need to have an item in your hand");
            return;
        }

        FileConfiguration config = plugin.getItemsFile().getConfig();

        String path = "items." + itemId + ".bytes";
        byte[] serializedItem = item.serializeAsBytes();

        String serializedBytes = Base64.getEncoder().encodeToString(serializedItem);

        config.set(path, serializedBytes);
        plugin.getItemsFile().saveConfig();
        player.sendMessage("Saved item as " + itemId);

        plugin.getItemManager().loadItems();
        /*ItemMeta meta = item.getItemMeta();
        MiniMessage mm = ComplexInteractions.miniMessage;

        config.set(path + ".material", item.getType().name());
        if (meta != null) {
            if (meta.hasDisplayName()) {
                String name = mm.serialize(meta.displayName());
                config.set(path + ".name", name);
            }

            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>();

                for (Component component : meta.lore()) {
                    lore.add(mm.serialize(component));
                }
                config.set(path + ".lore", lore);
            }

            if (meta.hasCustomModelData()) {
                config.set(path + ".custom_model_data", meta.getCustomModelData());
            }

            if (!meta.getItemFlags().isEmpty()) {
                List<String> flags = meta.getItemFlags().stream().map(Enum::name).toList();
                config.set(path + ".flags", flags);
            }

            if (item.getType() == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
                PlayerProfile profile = skullMeta.getPlayerProfile();
                if (profile != null) {
                    if (profile.getId() != null)
                        config.set(path + ".uuid", profile.getId().toString());

                    for (ProfileProperty property : profile.getProperties()) {
                        if (property.getName().equals("textures")) {
                            config.set(path + ".base64", property.getValue());
                            break;
                        }
                    }
                }

            }

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.getKeys().isEmpty()) {
                for (NamespacedKey key : pdc.getKeys()) {
                    if (pdc.has(key, PersistentDataType.STRING)) {
                        String value = pdc.get(key, PersistentDataType.STRING);
                        config.set(path + ".pdc." + key.toString(), value);
                    }
                }
            }
        }

         */
    }
}
