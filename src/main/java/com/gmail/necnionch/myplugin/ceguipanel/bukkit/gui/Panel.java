package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


public abstract class Panel {
    public static final Set<Panel> PANELS = new HashSet<>();
    public static Plugin OWNER;

    private final EventListener listener = new EventListener();
    private Inventory inventory;
    private final Player player;
    private final Map<ItemStack, PanelItem> cachedItems = new HashMap<>();
    private final ItemStack backgroundItem;
    private MessageHandler messageHandler;
    private MessageListener messageListener;
    private int lastInvSize;
    private String lastInvTitle;

    private Panel backPanel;


    public Panel(Player player, int size, String title, ItemStack background) {
//        this.inventory = Bukkit.createInventory(null, size, invTitle);
        this.player = player;
        backgroundItem = background;
        this.lastInvSize = size;
        this.lastInvTitle = title;
    }

    public Panel(Player player, int size, String title) {
        this(player, size, title, PanelItem.createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString()).getItemStack());
    }


    public void open() {
        placeItems(build());

        Bukkit.getPluginManager().registerEvents(listener, OWNER);
        player.openInventory(inventory);
        PANELS.add(this);
    }

    public void destroy(boolean close) {
        HandlerList.unregisterAll(listener);

        if (inventory != null && inventory.equals(player.getOpenInventory().getTopInventory()) && close) {
            player.closeInventory();
        }

        PANELS.remove(this);
    }

    public void destroy() {
        destroy(true);
    }

    public static void destroyAll() {
        new HashSet<>(PANELS).forEach(Panel::destroy);
    }


    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }


    public void placeItems(PanelItem[] items) {
        cachedItems.clear();
        if (inventory != null)
            inventory.clear();

        boolean reopen = false;
        int size = getSize();
        String title = getTitle();
        if (inventory == null || lastInvSize != size || !lastInvTitle.equals(title)) {
            reopen = inventory != null;
            inventory = Bukkit.createInventory(null, (size > 0) ? size : 9, (title != null) ? title : "");
            lastInvSize = size;
            lastInvTitle = title;
        }

        int index = 0;
        for (PanelItem item : items) {
            ItemStack itemStack = null;

            if (item != null)
                itemStack = item.getItemBuilder().build(player);

            if (itemStack != null) {
                cachedItems.put(itemStack, item);
            } else if (backgroundItem != null) {
                itemStack = backgroundItem.clone();
            }

            inventory.setItem(index, itemStack);
            index++;
        }

        if (reopen)
            player.openInventory(inventory);

    }

    private PanelItem selectPanelItem(ItemStack item) {
        return cachedItems.get(item);
    }

    public void setMessageHandler(MessageHandler listener) {
        if (messageListener != null) {
            HandlerList.unregisterAll(messageListener);
        }

        messageHandler = listener;
        if (listener != null) {
            messageListener = new MessageListener();
            Bukkit.getPluginManager().registerEvents(messageListener, OWNER);
        }
    }

    public int getSize() {
        return lastInvSize;
    }

    public String getTitle() {
        return lastInvTitle;
    }

    public Panel setBackPanel(Panel backPanel) {
        this.backPanel = backPanel;
        return this;
    }

    public Panel getBackPanel() {
        return backPanel;
    }

    public void close() {
        if (backPanel != null) {
            backPanel.open();
        } else {
            destroy(true);
        }
    }

    public PanelItem createBackButton() {
        return PanelItem.createItem(Material.BARRIER, "")
                .setClickListener((e, p) -> {
                    Panel backPanel = getBackPanel();
                    if (backPanel == null) {
                        destroy(true);
                    } else {
                        backPanel.open();
                        destroy(false);
                        playClickSound(p);
                    }
                }).setItemBuilder((p) -> {
                    Material icon;
                    String name;
                    Panel back = getBackPanel();
                    if (back != null) {
                        icon = Material.ACACIA_DOOR;
                        name = ChatColor.GOLD + ChatColor.stripColor(back.getTitle()) + "ページに戻る";
                    } else {
                        icon = Material.OAK_DOOR;
                        name = ChatColor.RED + "閉じる";
                    }
                    return PanelItem.createItem(icon, name).getItemStack();
                });
    }

    public void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, .75f, 2f);
    }


    abstract public PanelItem[] build();

    public boolean onClick(InventoryClickEvent event) {
        return false;
    }



    private class EventListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player))
                destroy();
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClose(InventoryCloseEvent event) {
            if (!inventory.equals(event.getInventory()))
                return;

            destroy(false);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onDrag(InventoryDragEvent event) {
            if (!inventory.equals(event.getInventory()))
                return;

            for (Integer slot : event.getRawSlots()) {
                if (inventory.getSize() > slot) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClick(InventoryClickEvent event) {
            if (!inventory.equals(event.getInventory()))
                return;

            if (InventoryAction.COLLECT_TO_CURSOR.equals(event.getAction()) && event.getCursor() != null) {
                if (Stream.of(inventory.getContents())
                        .anyMatch(i -> event.getCursor().isSimilar(i))) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            } else if (InventoryAction.MOVE_TO_OTHER_INVENTORY.equals(event.getAction())) {
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }

            if (!inventory.equals(event.getClickedInventory()))
                return;

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            if (!Panel.this.onClick(event)) {
                switch (event.getAction()) {
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                    case MOVE_TO_OTHER_INVENTORY:
                        break;
                    default:
                        return;
                }

                ItemStack current = event.getCurrentItem();
                PanelItem selected = selectPanelItem(current);

                if (selected != null)
                    selected.getClickListener().click(event, player);
            }

        }

    }

    private class MessageListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(getPlayer()))
                setMessageHandler(null);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            if (messageHandler != null && event.getPlayer().equals(getPlayer())) {
                event.setCancelled(true);

                Bukkit.getScheduler().runTask(OWNER, () -> {
                    if (messageHandler.onMessage(event.getMessage()))
                        setMessageHandler(null);
                });
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (messageHandler != null && event.getPlayer().equals(getPlayer())) {
                event.setCancelled(true);
                if (!messageHandler.onMessage(event.getMessage()))
                    return;
                setMessageHandler(null);
            }
        }

    }



    public interface MessageHandler {
        boolean onMessage(String message);
    }


}
