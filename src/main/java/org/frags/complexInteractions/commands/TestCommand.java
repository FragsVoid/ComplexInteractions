package org.frags.complexInteractions.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.ConversationStage;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class TestCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Component finalMessage = Component.text("hello").clickEvent(ClickEvent.callback((audience) -> {
            if (audience instanceof Player p) {
                p.sendMessage("clicked");
            }
        }, ClickCallback.Options.builder()
                .uses(1)
                .lifetime(Duration.ofMinutes(2))
                .build()));

        sender.sendMessage(finalMessage);

        return true;
    }
}
