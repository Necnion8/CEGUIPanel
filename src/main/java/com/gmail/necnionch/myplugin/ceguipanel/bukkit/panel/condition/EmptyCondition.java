package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EmptyCondition implements Condition {
    @Override
    public boolean check(GUIPanel panel, Player player) {
        return false;
    }

    @Override
    public @Nullable List<String> getDescription(Player player) {
        return null;
    }

    @Override
    public void serialize(Map<String, Object> config) {

    }


    public static class Creator implements ConditionCreator<EmptyCondition> {

        @Override
        public @NotNull EmptyCondition create() {
            return new EmptyCondition();
        }

        @Override
        public @Nullable EmptyCondition createFromConfig(Map<?, ?> config) {
            return null;
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return new ItemStack(Material.AIR);
        }

        @Override
        public @NotNull String getConditionId() {
            return "";
        }
    }
}
