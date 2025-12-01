package org.frags.complexInteractions.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.Conversation;
import org.frags.complexInteractions.objects.conversation.ConversationStage;
import org.frags.complexInteractions.objects.conversation.Option;

public class Session {

    private Conversation conversation;
    private ConversationStage stage;
    private Player player;

    public Session(Player player, Conversation conversation) {
        this.conversation = conversation;
        this.player = player;
        if (conversation == null) return;
        this.stage = conversation.getStage(conversation.getStartStageId());
    }

    public void startConversation(Conversation conversation) {
        this.conversation = conversation;
        this.stage = conversation.getStage(conversation.getStartStageId());
        startConversationStage(stage);
    }

    public void setStage(ConversationStage stage) {
        this.stage = stage;
        startConversationStage(stage);
    }

    private void startConversationStage(ConversationStage stage) {
        for (String message : stage.getText()) { //First we send the text
            player.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
        //Then we send the options
        for (Option option : stage.getOptionList()) {

            if (!option.hasRequirements(player)) continue;

            Component optionText = MiniMessage.miniMessage().deserialize(option.getText());

            Component finalMessage = optionText
                    .clickEvent(ClickEvent.runCommand(""));
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    public ConversationStage getStage() {
        return stage;
    }
}
