package com.gmail.necnionch.myplugin.ceguipanel.bukkit;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class GUIPanelPlugin extends JavaPlugin {
    private final GUIPanelManager panelManager = new GUIPanelManager(this);

    @Override
    public void onEnable() {
        new CommandAPICommand("ceguipanel")
                .withSubcommand(new CommandAPICommand("createpanel")
                        .executes(this::execCreatePanel)
                )
                .withSubcommand(new CommandAPICommand("removepanel")
                        .executes(this::execRemovePanel)
                )
                .withSubcommand(new CommandAPICommand("open")
                        .executes(this::execOpen)
                )
                .withSubcommand(new CommandAPICommand("edit")
                        .executes(this::execEdit)
                )
                .register();


        getServer().getServicesManager().register(GUIPanelManager.class, panelManager, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
    }


    private int execCreatePanel(CommandSender sender, Object[] objects) {
        return 0;
    }

    private int execRemovePanel(CommandSender sender, Object[] objects) {
        return 0;
    }

    private int execOpen(CommandSender sender, Object[] objects) {
        return 0;
    }

    private int execEdit(CommandSender sender, Object[] objects) {
        return 0;
    }






}
