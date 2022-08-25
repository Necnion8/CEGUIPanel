package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class NoneAction implements ClickAction {
    @Override
    public boolean action(GUIPanel panel, Player player) {
        return true;
    }


    public static class Creator implements ClickActionCreator<NoneAction> {

        @Override
        public @NotNull NoneAction create() {
            return new NoneAction();
        }

        @Override
        public @Nullable NoneAction createFromConfig(Map<?, ?> config) {
            return new NoneAction();
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.REDSTONE_TORCH, ChatColor.GOLD + "何もしない").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "none";
        }
    }

}
