package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ClickConditionCreator<C extends ClickCondition> {

    @NotNull C create();

    @Nullable
    C createFromConfig(Map<?, ?> config);

    @NotNull ItemStack getSelectIcon();

    @NotNull String getConditionId();

}
