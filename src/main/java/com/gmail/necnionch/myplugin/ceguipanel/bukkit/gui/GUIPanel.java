package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.PanelConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIPanel extends Panel {
    public GUIPanel(Player player, int size, String title, ItemStack background) {
        super(player, size, title, background);
    }

    @Override
    public PanelItem[] build() {
        return new PanelItem[0];
    }


    public static void create(Player player, PanelConfig config) {
        String title = config.getTitle();
        GUISize size = config.getSize();

        List<ItemConfig> items = config.getItems();


    }

}
