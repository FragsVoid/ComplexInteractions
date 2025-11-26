package org.frags.complexInteractions.objects.conversation.requirements;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.objects.conversation.interfaces.ItemMatcher;
import org.frags.complexInteractions.objects.conversation.Requirement;

public class ItemRequirement extends Requirement {

    private final ItemMatcher matcher;
    private final int amount;

    public ItemRequirement(String failMessage, ItemMatcher matcher, int amount) {
        super(failMessage);
        this.matcher = matcher;
        this.amount = amount;
    }

    @Override
    public boolean check(Player player) {
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && matcher.matches(item)) {
                count += item.getAmount();
            }
        }

        return count >= amount;
    }
}
