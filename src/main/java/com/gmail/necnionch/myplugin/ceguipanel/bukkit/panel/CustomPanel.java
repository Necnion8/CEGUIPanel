package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.PanelConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanelItem;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.Condition;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class CustomPanel extends GUIPanel {

    private final List<WeightItems<GUIPanelItem>> items;
    private BukkitTask updateTask;

    public CustomPanel(Player player, String title, GUISize size, ItemStack background, List<WeightItems<GUIPanelItem>> items) {
        super(player, title, size, background);
        this.items = items;
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[size.getSize()];

        for (WeightItems<GUIPanelItem> weights : items) {
            if (weights.getSlot() >= size.getSize())
                continue;

            for (GUIPanelItem item : weights.items()) {
                Condition cond = item.getViewCondition();
                if (cond != null && !cond.check(this, getPlayer()))
                    continue;
                slots[weights.getSlot()] = item;
            }
        }

        return slots;
    }

    @Override
    public void open() {
        super.open();
        if (updateTask != null)
            updateTask.cancel();
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(OWNER, this::update, 10, 10);
    }

    @Override
    public void destroy(boolean close) {
        if (updateTask != null)
            updateTask.cancel();
        super.destroy(close);
    }

    public static CustomPanel create(Player player, PanelConfig config) {
        String title = config.getTitle();
        GUISize size = config.getSize();

        List<WeightItems<GUIPanelItem>> items = Lists.newArrayList();
        CustomPanel panel = new CustomPanel(player, title, size, new ItemStack(Material.AIR), items);

        items.addAll(WeightItems.sortedOf(config.items(), (c) -> new GUIPanelItem(panel, c)));
        return panel;
    }

}
