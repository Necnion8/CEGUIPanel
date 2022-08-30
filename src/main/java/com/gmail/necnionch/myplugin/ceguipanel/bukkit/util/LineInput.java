package com.gmail.necnionch.myplugin.ceguipanel.bukkit.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.function.Consumer;

public class LineInput {

    private final Plugin plugin;
    private final Player player;
    private final Consumer<Optional<String>> complete;

    private final Listener eventListener = new Listener() {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            if (player.equals(event.getPlayer())) {
                plugin.getServer().getScheduler().runTask(plugin, () -> complete(event.getMessage()));
                event.setCancelled(true);
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (player.equals(event.getPlayer())) {
                plugin.getServer().getScheduler().runTask(plugin, () -> complete(null));
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (player.equals(event.getPlayer()))
                complete(null);
        }
    };

    public LineInput(Plugin plugin, Player player, Consumer<Optional<String>> complete) {
        this.plugin = plugin;
        this.player = player;
        this.complete = complete;

    }

    public LineInput listen() {
        plugin.getServer().getPluginManager().registerEvents(eventListener, plugin);
        return this;
    }

    private void complete(String result) {
        HandlerList.unregisterAll(eventListener);
        complete.accept(Optional.ofNullable(result));
    }

    public static void listen(Plugin plugin, Player player, Consumer<Optional<String>> complete) {
        new LineInput(plugin, player, complete).listen();
    }

}
