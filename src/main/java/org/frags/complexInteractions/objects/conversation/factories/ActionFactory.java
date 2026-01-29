package org.frags.complexInteractions.objects.conversation.factories;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.frags.complexInteractions.ComplexInteractions;
import org.frags.complexInteractions.objects.conversation.Action;
import org.frags.complexInteractions.objects.conversation.actions.*;
import org.frags.complexInteractions.objects.missions.MobProgress;
import org.frags.customItems.CustomItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionFactory {

    public static Action parse(String configLine) {

        if (configLine.startsWith("[walk]")) {
            // [walk]npcId;path;nextStageId;nextNpcId;world
            // [walk]icaro;100,64,200,world|105,64,200|105,64,210;conversation_llegada;guardia_real
            try {
                String params = configLine.replace("[walk]", "");
                String[] parts = params.split(";");

                if (parts.length < 3) {
                    Bukkit.getLogger().warning("[walk] Invalid format. Use: [walk]npcId;x,y,z|x,y,z;nextStage;nextNpc;world");
                    return null;
                }

                String npcId = parts[0];
                String pathString = parts[1];

                List<Location> path = new ArrayList<>();
                String[] points = pathString.split("\\|");

                org.bukkit.World lastWorld = Bukkit.getWorld(parts[4]);

                for (String point : points) {
                    String[] coords = point.split(",");
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    double z = Double.parseDouble(coords[2]);

                    if (coords.length > 3) {
                        org.bukkit.World w = Bukkit.getWorld(coords[3]);
                        if (w != null) lastWorld = w;
                    }

                    path.add(new Location(lastWorld, x, y, z));
                }

                String nextStageId = (parts.length > 2) ? parts[2] : null;
                String nextNpc = (parts.length > 3) ? parts[3] : null;

                return new WalkAction(ComplexInteractions.getInstance(), npcId, path, nextStageId, nextNpc);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Error parsing [walk] action: " + configLine);
                e.printStackTrace();
                return null;
            }
        }

        if (configLine.startsWith("[startquest]")) {
            // [startquest]questId;npcId;MOB_TYPE:CANTIDAD,MOB_TYPE:CANTIDAD
            // [startquest]caza_zombies;guardia_entrada;ZOMBIE:10,SKELETON:5
            try {
                String args = configLine.replace("[startquest]", "");
                String[] parts = args.split(";");

                if (parts.length < 3) {
                    Bukkit.getLogger().warning("[startquest] Invalid format. Use: [startquest]questId;npcId;MOB:amount");
                    return null;
                }

                String questId = parts[0];
                String npcId = parts[1];
                String mobsString = parts[2];

                Map<String, MobProgress> mobProgressMap = new HashMap<>();

                String[] mobs = mobsString.split(",");
                for (String mobEntry : mobs) {
                    String[] mobData = mobEntry.split(":");
                    String mobType = mobData[0].trim();
                    int amount = Integer.parseInt(mobData[1].trim());

                    mobProgressMap.put(mobType, new MobProgress(mobType, amount));
                }

                return new StartQuestAction(ComplexInteractions.getInstance(), npcId, questId, mobProgressMap);

            } catch (Exception e) {
                Bukkit.getLogger().warning("Error parsing [startquest] action: " + configLine);
                e.printStackTrace();
                return null;
            }
        }

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
                return new RemoveItemAction(CustomItems.INSTANCE.getItemProvider().getItem(parts[0]), Integer.parseInt(parts[1]), preset);
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

        if (configLine.startsWith("[giveitem]")) {
            String newName = configLine.replace("[giveitem]", "");
            if (newName.startsWith("preset:")) {
                String preset = newName.replace("preset:", "");
                String[] parts = preset.split(" ");
                return new GiveItemAction(CustomItems.INSTANCE.getItemProvider().getItem(parts[0]), Integer.parseInt(parts[1]), preset);
            } else {
                String[] parts = newName.split(" ");
                Material material = Material.matchMaterial(parts[0].toUpperCase());
                if (material == null) {
                    Bukkit.getLogger().warning("[giveitem] is not a valid material");
                    return null;
                }

                return new GiveItemAction(new ItemStack(material), Integer.parseInt(parts[1]), null);
            }
        }

        return null;
    }
}
