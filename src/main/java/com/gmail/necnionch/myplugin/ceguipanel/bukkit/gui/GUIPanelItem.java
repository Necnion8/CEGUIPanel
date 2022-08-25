package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GUIPanelItem extends PanelItem {

    private final @Nullable ItemStack iconItemStack;
    private final @NotNull ClickAction clickAction;

    public GUIPanelItem(@NotNull ClickAction clickAction, @Nullable ItemStack icon) {
        super(icon);
        this.clickAction = clickAction;
        this.iconItemStack = icon;
    }

    @Override
    public ItemStack getItemStack() {
        return super.getItemStack();
    }

    @Override
    public ItemBuilder getItemBuilder() {
        return super.getItemBuilder();
    }

    @Override
    public ClickEventListener getClickListener() {
        return super.getClickListener();
    }

    public @NotNull ClickAction getClickAction() {
        return clickAction;
    }

}
