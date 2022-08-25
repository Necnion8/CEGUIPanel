package com.gmail.necnionch.myplugin.ceguipanel.bukkit.config;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required.ClickCondition;
import org.jetbrains.annotations.Nullable;

public class ItemConfig {

    private final int slot;
    private final ClickAction clickAction;
    private final @Nullable ClickCondition clickCondition;
    private final @Nullable GUIIcon icon;

    public ItemConfig(int slot, ClickAction clickAction, @Nullable ClickCondition clickCondition, @Nullable GUIIcon icon) {
        this.slot = slot;
        this.clickAction = clickAction;
        this.clickCondition = clickCondition;
        this.icon = icon;
    }

    public int getSlot() {
        return slot;
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    public @Nullable ClickCondition getClickCondition() {
        return clickCondition;
    }

    public @Nullable GUIIcon getIcon() {
        return icon;
    }

}
