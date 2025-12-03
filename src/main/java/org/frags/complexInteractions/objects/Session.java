package org.frags.complexInteractions.objects;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.managers.SessionManager;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;
import org.frags.complexInteractions.objects.conversation.Option;

import java.time.Duration;
import java.util.List;

public class Session {

    private Conversation conversation;
    private ConversationStage stage;
    private final SessionManager sessionManager;
    private Player player;

    public Session(Player player, Conversation conversation, SessionManager sessionManager) {
        this.conversation = conversation;
        this.player = player;
        this.sessionManager = sessionManager;
    }

    public void start() {
        if (conversation == null) return;
        this.stage = conversation.getStage(conversation.getStartStageId());
        startConversationStage(stage);
    }

    public void startStage(ConversationStage stage) {
        this.stage = stage;
        startConversationStage(stage);
    }

    private void startConversationStage(ConversationStage stage) {
        List<String> messages = stage.getText();

        for (int i = 0; i < messages.size(); i++) {
            final String message = messages.get(i);
            Bukkit.getScheduler().runTaskLater(ComplexInteractions.getInstance(), () -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
            }, stage.getDelay() * i);
        }

        //Then we send the options
        for (Option option : stage.getOptionList()) {

            if (!option.hasRequirements(player)) continue;

            Component optionText = MiniMessage.miniMessage().deserialize(option.getText());

            Component finalMessage = optionText.clickEvent(ClickEvent.callback((audience) -> {
                if (audience instanceof Player p) {
                    option.getOnClickActions().forEach(a -> {a.execute(p);});

                    String nextId = option.getNextStage();
                    if (nextId == null) {
                        sessionManager.endSession(p);
                    } else {
                        ConversationStage nextStage = conversation.getStage(nextId);
                        if (nextStage != null) {
                            this.startStage(nextStage);
                        } else {
                            sessionManager.endSession(p);
                        }
                    }
                }
            }, ClickCallback.Options.builder()
                    .uses(1)
                    .lifetime(Duration.ofMinutes(2))
                    .build()));


            player.sendMessage(finalMessage);
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    public ConversationStage getStage() {
        return stage;
    }
}
