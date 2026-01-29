package org.frags.complexInteractions.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.missions.ActiveQuest;
import org.frags.complexInteractions.objects.missions.MobProgress;
import org.frags.customItems.menu.Menu;
import org.frags.customItems.menu.PaginatedMenu;
import org.frags.customItems.menu.PlayerMenuUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestMenu extends Menu<ComplexInteractions, PlayerMenuUtility> {

    public QuestMenu(ComplexInteractions plugin, PlayerMenuUtility playerMenuUtility) {
        super(plugin, playerMenuUtility);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Misiones Activas");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }

        if (e.getCurrentItem().getType() == Material.BARRIER) {
            e.getWhoClicked().closeInventory();
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getPlayer();
        addMenuBorder();
        Map<String, ActiveQuest> quests = plugin.getQuestManager().getQuests(player);

        if (quests == null || quests.isEmpty()) {
            inventory.setItem(22, makeItem(Material.BOOK, Component.text("No tienes misiones activas", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            addCloseButton();
            return;
        }

        for (ActiveQuest quest : quests.values()) {
            Conversation conv = plugin.getConversationManager().getConversation(quest.getNpcId());
            Material icon = (conv != null && conv.getIcon() != null) ? conv.getIcon() : Material.PAPER;
            Component name = (conv != null && conv.getMissionName() != null)
                    ? conv.getMissionName()
                    : Component.text(quest.getQuestId());

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(name);

            List<Component> lore = new ArrayList<>();
            
            lore.add(Component.text("Progreso:", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

            for (MobProgress progress : quest.getMobList().values()) {
                NamedTextColor color = progress.isComplete() ? NamedTextColor.GREEN : NamedTextColor.WHITE;
                String check = progress.isComplete() ? "✔ " : "  ";

                lore.add(Component.text(check + formatMobName(progress.getMobType()) + ": ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(progress.getCurrentAmount() + "/" + progress.getRequiredAmount(), color).decoration(TextDecoration.ITALIC, false)));
            }

            lore.add(Component.empty());

            if (quest.isComplete()) {
                lore.add(Component.text("Completada", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add(Component.text("En progreso...", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            }

            meta.lore(lore);
            item.setItemMeta(meta);

            inventory.addItem(item);
        }

        addCloseButton();
    }

    private void addCloseButton() {
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        meta.displayName(Component.text("Cerrar", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(meta);
        inventory.setItem(49, close);
    }

    private ItemStack makeItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private String formatMobName(String input) {
        if (input == null) return "";
        String[] words = input.toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0)));
                builder.append(word.substring(1));
                builder.append(" ");
            }
        }
        return builder.toString().trim();
    }

    private void addMenuBorder() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);

        int size = getSlots();

        for (int i = 0; i < size; i++) {
            boolean isTop = i < 9;
            boolean isBottom = i >= (size - 9);
            boolean isLeft = i % 9 == 0;
            boolean isRight = i % 9 == 8;

            if ((isTop || isBottom || isLeft || isRight) && inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }
}
