package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CloseAction implements ClickAction {
    @Override
    public boolean action(GUIPanel panel, Player player) {
        if (panel.getBackPanel() != null) {
            panel.getBackPanel().open();
        } else {
            panel.destroy(true);
        }
        return true;
    }

    @Override
    public List<String> getDescription(Player player) {
        return Lists.newArrayList("前のページに戻る");
    }

    @Override
    public void serialize(Map<String, Object> config) {
    }


    public static class Creator implements ClickActionCreator<CloseAction> {

        @Override
        public @NotNull CloseAction create() {
            return new CloseAction();
        }

        @Override
        public @Nullable CloseAction createFromConfig(Map<?, ?> config) {
            return new CloseAction();
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.OAK_DOOR, ChatColor.GOLD + "前のページに戻る").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "close";
        }
    }

}
