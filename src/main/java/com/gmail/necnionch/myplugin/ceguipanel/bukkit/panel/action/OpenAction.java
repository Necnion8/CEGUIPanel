package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class OpenAction implements ClickAction {

    private @Nullable String panel;

    public OpenAction(@Nullable String panel) {
        this.panel = panel;
    }

    public @Nullable String getPanelName() {
        return panel;
    }

    public void setPanelName(@Nullable String panel) {
        this.panel = panel;
    }

    public @Nullable GUIPanel openPanel(Player player) {
        if (panel != null) {
            GUIPanelManager mgr = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(GUIPanelManager.class)).getProvider();
            return mgr.createGUIPanelByName(panel, player);
        }
        return null;
    }

    @Override
    public boolean action(GUIPanel panel, Player player) {
        return openPanel(player) != null;
    }

//    @Override
//    public String getName() {
//        return "パネルを開く";
//    }


    public static class Creator implements ClickActionCreator<OpenAction> {

        @Override
        public @NotNull OpenAction create() {
            return new OpenAction(null);
        }

        @Override
        public @Nullable OpenAction createFromConfig(Map<?, ?> config) {
            Object panel = config.get("panel");
            return new OpenAction(panel instanceof String ? (String) panel : null);
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.CHEST, ChatColor.GOLD + "パネルを開く").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "open";
        }
    }

}
