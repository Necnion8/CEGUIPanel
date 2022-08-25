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

public class CloseAction implements ClickAction {
    @Override
    public boolean action(GUIPanel panel, Player player) {
        panel.close();
        return true;
    }

//    @Override
//    public String getName() {
//        return "閉じる/前のページに戻る";
//    }


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
            return PanelItem.createItem(Material.OAK_DOOR, ChatColor.GOLD + "閉じる/前のページに戻る").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "close";
        }
    }

}
