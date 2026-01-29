package org.frags.complexInteractions.objects;


import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.managers.SessionManager;
import org.frags.complexInteractions.objects.conversation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Session {

    private static final NamespacedKey FREEZE_MODIFIER_KEY = new NamespacedKey("freeze_modifier_cinteractions", "freeze_modifier");
    private static final AttributeModifier FREEZE_MODIFIER = new AttributeModifier(FREEZE_MODIFIER_KEY, -1.0,
            AttributeModifier.Operation.ADD_SCALAR);

    private Conversation conversation;
    private ConversationStage stage;
    private final SessionManager sessionManager;
    private Player player;
    private boolean ended = false;

    private final ComplexInteractions plugin;

    private boolean isTurningInQuest = false;

    private boolean waitingForMovement = false;

    public Session(ComplexInteractions plugin, Player player, Conversation conversation, SessionManager sessionManager) {
        this.conversation = conversation;
        this.player = player;
        this.sessionManager = sessionManager;
        this.plugin = plugin;
    }

    public void start() {
        if (conversation == null) return;

        for (Requirement requirement : conversation.getRequirements()) {
            if (!requirement.check(player)) {
                this.stage = conversation.getStage(conversation.getNoReqStageId());
                startConversationStage(stage);
                return;
            }
        }

        this.stage = conversation.getStage(conversation.getStartStageId());
        if (conversation.isBlockMovement()) {
            freezePlayer();
        }
        startConversationStage(stage);
    }

    public void startStage(ConversationStage stage) {
        this.stage = stage;
        startConversationStage(stage);
    }

    private void startConversationStage(ConversationStage stage) {
        if (stage.getStartActions() != null) {
            for (Action action : stage.getStartActions()) {
                action.execute(player);
            }
        }

        List<String> messages = stage.getText();

        for (int i = 0; i < messages.size(); i++) {
            if (isEnded()) {
                sessionManager.endSession(player, false);
                return;
            }
            final String message = messages.get(i);
            long left = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), conversation.getNpcId());
            Bukkit.getScheduler().runTaskLater(ComplexInteractions.getInstance(), () -> {
                String parsedMessage = PlaceholderAPI.setPlaceholders(player, message)
                        .replace("%player%", player.getName())
                        .replace("%time%", sessionManager.getRemainingTimeFormatted(left));
                player.sendMessage(MiniMessage.miniMessage().deserialize(parsedMessage));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }, stage.getDelay() * i * 20);
        }

        long totalDuration = stage.getDelay() * messages.size() * 20;

        if (stage.getOptionList() == null || stage.getOptionList().isEmpty()) {
            Bukkit.getScheduler().runTaskLater(ComplexInteractions.getInstance(), () -> {
                if (!waitingForMovement) {
                    sessionManager.endSession(player, true);
                }
            }, totalDuration);
        }

        Bukkit.getScheduler().runTaskLater(ComplexInteractions.getInstance(), () -> {
            if (isEnded()) {
                sessionManager.endSession(player, false);
                return;
            }
            manageActions(stage);
            manageOptions(stage);
        }, stage.getDelay() * messages.size() * 20);

    }

    private void manageActions(ConversationStage stage) {
        List<Action> actions = stage.getActions();

        for (Action action : actions) {
            action.execute(player);
        }
    }

    private void manageOptions(ConversationStage stage) {
        List<Option> options = stage.getOptionList();

        AtomicBoolean alreadyClicked = new AtomicBoolean(false);

        for (int i = 0; i < options.size(); i++) {
            if (isEnded()) {
                sessionManager.endSession(player, false);
                return;
            }
            Option option = options.get(i);
            if (!sessionManager.isConversing(player)) return;

            String parsedMessage = PlaceholderAPI.setPlaceholders(player, option.getText());

            Component optionText = MiniMessage.miniMessage().deserialize(parsedMessage);

            Component finalMessage = optionText.clickEvent(ClickEvent.callback((audience) -> {
                if (alreadyClicked.get()) return;
                alreadyClicked.set(true);
                if (audience instanceof Player p) {
                    if (!option.hasRequirements(p)) {
                        ConversationStage nextStage = conversation.getStage(option.getNoRequirementId());
                        if (nextStage != null) {
                            this.startStage(nextStage);
                        } else {
                            sessionManager.endSession(player, false);
                        }
                        return;
                    }

                    option.getOnClickActions().forEach(a -> {a.execute(p);});
                    String nextId = option.getNextStage();
                    if (nextId == null) {
                        sessionManager.endSession(p, true);
                    } else {
                        ConversationStage nextStage = conversation.getStage(nextId);
                        if (nextStage != null) {
                            this.startStage(nextStage);
                        } else {
                            sessionManager.endSession(p, true);
                        }
                    }
                }
            }, ClickCallback.Options.builder()
                    .uses(1)
                    .lifetime(Duration.ofMinutes(1))
                    .build()));


            player.sendMessage(finalMessage);
        }
    }

    public boolean isWaitingForMovement() {
        return waitingForMovement;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
        if (ended) {
            sessionManager.endSession(player, false);
        }
    }

    public void setWaitingForMovement(boolean waitingForMovement) {
        this.waitingForMovement = waitingForMovement;
    }

    public void setTurningInQuest(boolean turningInQuest) {
        isTurningInQuest = turningInQuest;
    }

    public boolean isTurningInQuest() {
        return isTurningInQuest;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public ConversationStage getStage() {
        return stage;
    }

    private void freezePlayer() {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute != null && !attribute.getModifiers().contains(FREEZE_MODIFIER)) {
            attribute.addModifier(FREEZE_MODIFIER);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100000, 250, false, false, false));
    }

    public void unfreezePlayer() {
        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(FREEZE_MODIFIER);
        }

        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    public void cleanup() {
        unfreezePlayer();
    }
}
