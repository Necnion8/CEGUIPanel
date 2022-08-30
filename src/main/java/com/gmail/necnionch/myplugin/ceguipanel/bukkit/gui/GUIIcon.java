package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms.NMSHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GUIIcon {

    private @NotNull ItemStack itemStack;

    public GUIIcon(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @NotNull ItemStack buildItemStack(Player player) {
        ItemStack itemStack = this.itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            if (meta.hasDisplayName()) {
                meta.setDisplayName(formatTellrawJson(player, meta.getDisplayName()));
            }

            Optional.ofNullable(meta.getLore()).ifPresent((lore) -> {
                List<String> formatted = lore.stream()
                        .map(s -> formatTellrawJson(player, s))
                        .collect(Collectors.toList());
                meta.setLore(formatted);
            });

            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }


    public GUIIcon copy() {
        return new GUIIcon(itemStack.clone());
    }


    private String formatTellrawJson(CommandSender sender, String string) {
        try {
            return NMSHandler.getNMS().formatTellrawJson(sender, string);
        } catch (Throwable e) {
            return ChatColor.translateAlternateColorCodes('&', string);
        }
    }

}
