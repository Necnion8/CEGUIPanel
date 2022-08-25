package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ClickActionCreator<A extends ClickAction> {

    @NotNull A create();

    @Nullable
    A createFromConfig(Map<?, ?> config);

    @NotNull ItemStack getSelectIcon();

    @NotNull String getActionId();


}
