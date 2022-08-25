package com.gmail.necnionch.myplugin.ceguipanel.bukkit.test;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OpenListener implements Listener {

    private final OpenPlayerPlugin plugin;

    public OpenListener(OpenPlayerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        OpenPlayer openPlayer = plugin.getInventories().get(event.getInventory());

        if (openPlayer == null)
            return;  // 知らないインベントリ

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        openPlayer.handleClickEvent(event);

    }



}
