package org.frags.complexInteractions.menu.menucache;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class MissionCache {

    private Component menuTitle;
    private int slots;
    private Component notAbleMessage;

    public void load(ConfigurationSection section) {
        if (section == null) return;

        MiniMessage mm = MiniMessage.miniMessage();

        this.menuTitle = mm.deserialize(section.getString("title", "Menu"));
        this.slots = section.getInt("slots", 54);
        this.notAbleMessage = mm.deserialize(section.getString("not_able", ""));
    }

    public Component getMenuTitle() { return menuTitle; }
    public int getSlots() { return slots; }
    public Component getNotAbleMessage() { return notAbleMessage; }
}
