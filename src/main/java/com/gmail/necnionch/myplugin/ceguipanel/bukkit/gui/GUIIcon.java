package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GUIIcon {

    private @NotNull
    final String display;
    private final @NotNull ItemStack itemStack;
    private final @NotNull List<String> lores;

    public GUIIcon(@NotNull String display, @NotNull List<String> lores, @NotNull ItemStack itemStack) {
        this.display = display;
        this.itemStack = itemStack;
        this.lores = Lists.newArrayList(lores);
    }

    public @NotNull String getDisplay() {
        return display;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @NotNull List<String> lores() {
        return lores;
    }

}
