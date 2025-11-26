package org.frags.complexInteractions.objects.conversation.actions;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.frags.complexInteractions.objects.conversation.Action;

public class SoundAction extends Action {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAction(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public boolean execute(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
        return true;
    }
}
