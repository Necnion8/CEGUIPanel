package com.gmail.necnionch.myplugin.ceguipanel.bukkit.config;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfig {

    private int slot;
    private ClickAction clickAction;
    private @Nullable Condition clickCondition;
    private @NotNull GUIIcon icon;
    private int weight;
    private @Nullable Condition viewCondition;

    public ItemConfig(int slot, ClickAction clickAction, @Nullable Condition clickCondition, @Nullable Condition viewCondition, @NotNull GUIIcon icon, int weight) {
        this.slot = slot;
        this.clickAction = clickAction;
        this.clickCondition = clickCondition;
        this.viewCondition = viewCondition;
        this.icon = icon;
        this.weight = weight;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    public @Nullable Condition getClickCondition() {
        return clickCondition;
    }

    public @Nullable Condition getViewCondition() {
        return viewCondition;
    }

    public @NotNull GUIIcon getIcon() {
        return icon;
    }

    public int getWeight() {
        return weight;
    }

    public void setClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
    }

    public void setClickCondition(@Nullable Condition clickCondition) {
        this.clickCondition = clickCondition;
    }

    public void setViewCondition(@Nullable Condition viewCondition) {
        this.viewCondition = viewCondition;
    }

    public void setIcon(@NotNull GUIIcon icon) {
        this.icon = icon;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}
