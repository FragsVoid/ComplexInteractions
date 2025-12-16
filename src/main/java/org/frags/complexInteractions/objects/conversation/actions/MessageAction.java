package org.frags.complexInteractions.objects.conversation.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.Action;

public class MessageAction extends Action {

    private final Component message;

    public MessageAction(Component message) {
        this.message = message;
    }

    @Override
    public boolean execute(Player player) {
        player.sendMessage(message);
        return true;
    }

    @Override
    public String toString() {
        return "[message]" + MiniMessage.miniMessage().serialize(message);
    }
}
