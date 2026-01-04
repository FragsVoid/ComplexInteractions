package org.frags.complexInteractions.menu.menucache;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DefaultCache {

    private CachedItem backItem;
    private CachedItem nextItem;
    private CachedItem closeItem;
    private ItemStack fillerItem;

    public void load(ConfigurationSection section) {
        if (section == null) return;

        MiniMessage mm = MiniMessage.miniMessage();

        this.backItem = loadItem(section.getConfigurationSection("back_item"), mm);
        this.nextItem = loadItem(section.getConfigurationSection("next_item"), mm);
        this.closeItem = loadItem(section.getConfigurationSection("close_item"), mm);

        ConfigurationSection fillerSec = section.getConfigurationSection("filler_item");
        if (fillerSec != null) {
            Material mat = Material.getMaterial(fillerSec.getString("material", "GRAY_STAINED_GLASS_PANE"));
            this.fillerItem = new ItemStack(mat);
            ItemMeta meta = this.fillerItem.getItemMeta();
            meta.displayName(mm.deserialize(fillerSec.getString("name", " ")));
            this.fillerItem.setItemMeta(meta);
        }
    }

    private CachedItem loadItem(ConfigurationSection section, MiniMessage mm) {
        if (section == null) return null;

        Material material = Material.getMaterial(section.getString("material", "STONE"));
        int slot = section.getInt("slot");
        String name = section.getString("name", "");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }

        return new CachedItem(item, slot);
    }

    public CachedItem getBackItem() { return backItem; }
    public CachedItem getNextItem() { return nextItem; }
    public CachedItem getCloseItem() { return closeItem; }

    public ItemStack getFillerItem() {
        return fillerItem.clone();
    }
}
