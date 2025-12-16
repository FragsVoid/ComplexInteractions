package org.frags.complexInteractions.objects.conversation.factories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Action;
import org.frags.complexInteractions.objects.conversation.actions.*;

public class ActionFactory {

    public static Action parse(String configLine) {

        if (configLine.startsWith("[console]")) {
            //[console]gamemode 1 %player%
            String cmd = configLine.replace("[console]", "");
            return new CommandAction(cmd, true);
        }

        if (configLine.startsWith("[player]")) {
            //[player]rtp
            String cmd = configLine.replace("[player]", "");
            return new CommandAction(cmd, false);
        }

        if (configLine.startsWith("[sound]")) {
            //[sound]SOUND_NAME,vol,pitch
            String[] parts = configLine.replace("[sound]", "").split(",");
            try {
                Sound sound = Sound.valueOf(parts[0].toUpperCase());
                float vol = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                return new SoundAction(sound, vol, pitch);
            } catch (Exception ex) {
                Bukkit.getLogger().warning("[sound] is not a valid sound");
                return null;
            }
        }

        if (configLine.startsWith("[addmoney]")) {
            //[addmoney]120(cantidad)
            double amount = Double.parseDouble(configLine.replace("[addmoney]", ""));
            return new AddMoneyAction(amount);
        }

        if (configLine.startsWith("[removemoney]")) {
            //[removemoney]123
            double amount = Double.parseDouble(configLine.replace("[removemoney]", ""));
            return new RemoveMoneyAction(ComplexInteractions.getInstance(),  amount);
        }

        if (configLine.startsWith("[message]")) {
            //[message]<red>Hello!
            String message =  configLine.replace("[message]", "");
            return new MessageAction(ComplexInteractions.miniMessage.deserialize(message));
        }

        if (configLine.startsWith("message:")) {
            String message = configLine.replace("message:", "");
            return new MessageAction(ComplexInteractions.miniMessage.deserialize(message));
        }

        if (configLine.startsWith("[removeitem]")) {
            //[removeitem]preset:cabeza2 100
            //[removeitem]DIAMOND 10
            String newName = configLine.replace("[removeitem]", "");
            if (newName.startsWith("preset:")) {
                String preset = newName.replace("preset:", "");
                String[] parts = preset.split(" ");
                return new RemoveItemAction(ComplexInteractions.getInstance().getItemManager().getItem(parts[0]), Integer.parseInt(parts[1]), preset);
            } else {
                String[] parts = newName.split(" ");
                Material material = Material.matchMaterial(parts[0].toUpperCase());
                if (material == null) {
                    Bukkit.getLogger().warning("[removeitem] is not a valid material");
                    return null;
                }

                return new RemoveItemAction(new ItemStack(material), Integer.parseInt(parts[1]), null);
            }
        }

        return null;
    }
}
