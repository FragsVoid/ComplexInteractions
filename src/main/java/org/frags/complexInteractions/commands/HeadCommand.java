package org.frags.complexInteractions.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.frags.complexInteractions.ComplexInteractions;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HeadCommand implements CommandExecutor {

    private ComplexInteractions plugin;

    public HeadCommand(ComplexInteractions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("interactions.head")) {
            player.sendMessage(ComplexInteractions.miniMessage.deserialize(plugin.getMessage("no_permission")));
            return true;
        }

        String value = args[0];
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta itemMeta = itemStack.getItemMeta();
        SkullMeta skullMeta = (SkullMeta) itemMeta;

        applyTexture(skullMeta, value);

        itemStack.setItemMeta(skullMeta);

        player.getInventory().addItem(itemStack);


        return true;
    }

    private void applyTexture(SkullMeta meta, String value) {
        if (value == null || value.isEmpty()) return;



        PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(value.getBytes()));

        if (value.startsWith("http")) {
            PlayerTextures textures = profile.getTextures();
            try {
                textures.setSkin(new URL(value));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            profile.setTextures(textures);
            meta.setPlayerProfile(profile);
            return;
        }

        profile.setProperty(new ProfileProperty("textures", value));

        meta.setPlayerProfile(profile);
    }
}
