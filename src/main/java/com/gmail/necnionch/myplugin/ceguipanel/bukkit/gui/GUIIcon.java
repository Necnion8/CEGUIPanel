package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

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

    public @NotNull ItemStack buildItemStack(Player player) {  // TODO: Tellraw JSON でフォーマットする
        ItemStack itemStack = this.itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
//            meta.setDisplayName(Optional.ofNullable(display)
//                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
//                    .orElse(null));
//            meta.setLore(lore.stream()
//                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
//                    .collect(Collectors.toList()));
//            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }


    public GUIIcon copy() {
        return new GUIIcon(itemStack.clone());
    }

}
