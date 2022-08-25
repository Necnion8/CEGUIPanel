package com.gmail.necnionch.myplugin.ceguipanel.bukkit.test;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class OpenPlayer {

    private final Player player;
    private final Inventory inventory;

    public OpenPlayer(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;

    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }


    public void handleClickEvent(InventoryClickEvent event) {
        // TODO: ここでイベントを処理する

    }

}
