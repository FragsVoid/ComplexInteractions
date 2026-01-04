package org.frags.complexInteractions.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.menu.menucache.CachedItem;
import org.frags.complexInteractions.menu.menucache.DefaultCache;
import org.frags.complexInteractions.menu.menucache.MissionCache;
import org.frags.complexInteractions.objects.conversation.*;
import org.frags.complexInteractions.objects.conversation.requirements.ConditionRequirement;
import org.frags.complexInteractions.objects.conversation.requirements.PermissionRequirement;
import org.frags.customItems.menu.Menu;
import org.frags.customItems.menu.PaginatedMenu;
import org.frags.customItems.menu.PlayerMenuUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissionMenu extends PaginatedMenu<ComplexInteractions, MissionMenuUtility> {


    public MissionMenu(ComplexInteractions plugin, MissionMenuUtility playerMenuUtility, int page) {
        super(plugin, playerMenuUtility, page);
    }


    @Override
    public Component getMenuName() {
        return plugin.getMenuManager().getMissionCache().getMenuTitle();
    }

    @Override
    public int getSlots() {
        return plugin.getMenuManager().getMissionCache().getSlots();
    }

    @Override
    public void handleMenu(InventoryClickEvent inventoryClickEvent) {
        int slot = inventoryClickEvent.getRawSlot();

        DefaultCache cache = plugin.getMenuManager().getDefaultCache();

        if (slot == cache.getBackItem().getSlot()) {
            page--;
            setMenuItems();
        } else if (slot == cache.getNextItem().getSlot()) {
            page++;
            setMenuItems();
        } else if (slot == cache.getCloseItem().getSlot()) {
            playerMenuUtility.getPlayer().closeInventory();
        } else if (slot == 52) {
            MissionFilter current = playerMenuUtility.getMissionFilter();
            playerMenuUtility.setMissionFilter(current.next());

            this.page = 0;
            setMenuItems();
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent inventoryCloseEvent) {

    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        Player player = playerMenuUtility.getPlayer();
        MissionCache cache = plugin.getMenuManager().getMissionCache();
        MissionFilter missionFilter = playerMenuUtility.getMissionFilter();

        List<Conversation> availableMissions = new ArrayList<>();

        for (Conversation c : plugin.getConversationManager().getMissions()) {
            // Filtro base: Solo una vez
            if (c.isOnlyOnce() && plugin.getSessionManager().hasCompleted(player.getUniqueId(), c)) {
                continue;
            }

            boolean matches = false;
            switch (missionFilter) {
                case ALL:
                    matches = true;
                    break;
                case MAIN_QUEST:
                    matches = isMainQuest(c);
                    break;
                case SIDE_QUEST:
                    matches = !isMainQuest(c);
                    break;
                case AVAILABLE:
                    matches = canPlayerStartMission(player, c);
                    break;
                case COOLDOWN:
                    matches = (c.getCooldown() > 0) && plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), c.getNpcId());
                    break;
            }

            if (matches) {
                availableMissions.add(c);
            }
        }
        

        availableMissions.sort((c1, c2) -> {
            boolean c1Can = canPlayerStartMission(player, c1);
            boolean c2Can = canPlayerStartMission(player, c2);

            if (c1Can != c2Can) {
                return c1Can ? -1 : 1;
            }

            boolean c1IsMain = isMainQuest(c1);
            boolean c2IsMain = isMainQuest(c2);

            if (c1IsMain != c2IsMain) {
                return c1IsMain ? -1 : 1;
            }


            String name1 = PlainTextComponentSerializer.plainText().serialize(c1.getMissionName());
            String name2 = PlainTextComponentSerializer.plainText().serialize(c2.getMissionName());

            return name1.compareToIgnoreCase(name2);
        });

        int itemsPerPage = getMaxItemsPerPage();
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availableMissions.size());

        addMenuBorder(availableMissions.size() > endIndex);

        for (int i = startIndex; i < endIndex; i++) {
            Conversation conversation = availableMissions.get(i);

            ItemStack item = new ItemStack(conversation.getIcon() != null ? conversation.getIcon() : Material.PAPER);

            ItemMeta meta = item.getItemMeta();

            meta.displayName(conversation.getMissionName());
            List<Component> lore = new ArrayList<>(conversation.getMissionLore());

            boolean canStart = checkRequirements(player, conversation);
            boolean isOnCooldown = false;
            long cooldownSeconds = 0;

            if (conversation.getCooldown() > 0) {
                cooldownSeconds = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), conversation.getNpcId());
                if (cooldownSeconds > 0) isOnCooldown = true;
            }

            if (!canStart) {
                lore.add(cache.getNotAbleMessage());
            } else if (isOnCooldown) {
                lore.add(ComplexInteractions.miniMessage.deserialize(
                        "<red>No puedes realizar esta misión en <white>" +
                                plugin.getSessionManager().getRemainingTimeFormatted(cooldownSeconds)
                ).decoration(TextDecoration.ITALIC, false));
            }

            meta.lore(lore);

            item.setItemMeta(meta);

            inventory.addItem(item);
        }
    }

    private void addMenuBorder(boolean hasNextPage) {
        DefaultCache cache = plugin.getMenuManager().getDefaultCache();

        CachedItem back = cache.getBackItem();
        CachedItem next = cache.getNextItem();
        CachedItem close = cache.getCloseItem();
        ItemStack filler = cache.getFillerItem();

        inventory.setItem(close.getSlot(), close.getItemStack());

        if (page > 0) {
            inventory.setItem(back.getSlot(), back.getItemStack());
        }

        if (hasNextPage) {
            inventory.setItem(next.getSlot(), next.getItemStack());
        }

        addFilterItem();

        int size = inventory.getSize();
        int lastRowIndex = size - 9;

        for (int i = 0; i < size; i++) {
            boolean isBorder = (i < 9) || (i >= lastRowIndex) || (i % 9 == 0) || (i % 9 == 8);

            if (isBorder && inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private void addFilterItem() {
        MissionFilter current = playerMenuUtility.getMissionFilter();

        ItemStack filterItem = new ItemStack(Material.HOPPER);
        ItemMeta meta = filterItem.getItemMeta();
        meta.displayName(plugin.miniMessage.deserialize("<gold>Filtro de Misiones"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        for (MissionFilter type : MissionFilter.values()) {
            String prefix = (type == current) ? "<green>▶ " : "<gray>";
            String suffix = (type == current) ? " <green>◀" : "";

            lore.add(plugin.miniMessage.deserialize(prefix + type.getDisplayName() + suffix)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(plugin.miniMessage.deserialize("<yellow>Clic para cambiar filtro")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);

        filterItem.setItemMeta(meta);

        inventory.setItem(52, filterItem);
    }

    private boolean canPlayerStartMission(Player player, Conversation c) {
        if (!checkRequirements(player, c)) return false;

        if (c.getCooldown() > 0) {
            if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), c.getNpcId())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkRequirements(Player player, Conversation c) {
        if (c.getRequirements() == null) return true;
        for (Requirement req : c.getRequirements()) {
            if (req instanceof ConditionRequirement || req instanceof PermissionRequirement) {
                if (!req.check(player)) return false;
            }
        }
        return true;
    }

    private boolean isMainQuest(Conversation c) {
        if (c.getMissionCategory() == null) return false;
        return c.getMissionCategory() == MissionCategory.QUEST;
    }
}
