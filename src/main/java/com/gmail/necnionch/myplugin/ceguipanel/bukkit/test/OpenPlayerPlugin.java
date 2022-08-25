package com.gmail.necnionch.myplugin.ceguipanel.bukkit.test;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OpenPlayerPlugin extends JavaPlugin {
    private final Map<Inventory, OpenPlayer> inventories = Maps.newHashMap();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new OpenListener(this), this);

        getCommand("samplegui").setExecutor(this::executeCommand);
    }

    public Map<Inventory, OpenPlayer> getInventories() {
        return inventories;
    }

    private boolean executeCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        Inventory inv = Bukkit.createInventory(null, 54);
        OpenPlayer openPlayer = new OpenPlayer(player, inv);
        inventories.put(inv, openPlayer);

        return true;
    }


}
