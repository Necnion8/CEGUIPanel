package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.Condition;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GUIPanelItem extends PanelItem {

    private final @NotNull GUIIcon icon;
    private final @NotNull ClickAction clickAction;
    private final @Nullable Condition clickCondition;
    private final @Nullable Condition viewCondition;
    private final int slot;
    private final @NotNull GUIPanel panel;
    private final int weight;
    private final @NotNull ItemConfig config;


    public GUIPanelItem(@NotNull GUIPanel panel, @NotNull ItemConfig config) {
        super(new ItemStack(Material.AIR));
        this.panel = panel;
        this.clickAction = config.getClickAction();
        this.clickCondition = config.getClickCondition();
        this.viewCondition = config.getViewCondition();
        this.icon = config.getIcon();
        this.slot = config.getSlot();
        this.weight = config.getWeight();
        this.config = config;

        setClickListener(this::handleClick);
        setItemBuilder(icon::buildItemStack);
    }

    public @NotNull ClickAction getClickAction() {
        return clickAction;
    }

    public @Nullable Condition getClickCondition() {
        return clickCondition;
    }

    public @Nullable Condition getViewCondition() {
        return viewCondition;
    }

    public @NotNull GUIIcon getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public int getWeight() {
        return weight;
    }

    public @NotNull ItemConfig getConfig() {
        return config;
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        if (!ClickType.LEFT.equals(event.getClick()))
            return;

        if (clickCondition != null && !clickCondition.check(panel, player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0);
            return;
        }

        if (clickAction.action(panel, player)) {
            if (clickCondition != null)
                clickCondition.complete(panel, player);
            panel.update();
        }
    }

}
