package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GUIPanel extends Panel {
    protected final GUISize size;
    protected final String title;
    protected final Player player;

    public GUIPanel(Player player, String title, GUISize size, @Nullable ItemStack background) {
        super(player, size.getType(), size.getSize(), title, background);
        this.player = player;
        this.title = title;
        this.size = size;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public GUISize getGUISize() {
        return size;
    }


    public static class WeightItems<T> {
        private final List<T> items;
        private int slot;

        public WeightItems(int slot, List<T> items) {
            this.items = items;
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public List<T> items() {
            return items;
        }


        public static <T> List<WeightItems<T>> sortedOf(List<ItemConfig> items, Function<ItemConfig, T> convert) {
            Map<Integer, Map<ItemConfig, T>> tmpMap = Maps.newHashMap();

            items.forEach(c -> {
                T entry = convert.apply(c);
                if (tmpMap.containsKey(c.getSlot())) {
                    tmpMap.get(c.getSlot()).put(c, entry);
                } else {
                    Map<ItemConfig, T> map = Maps.newHashMap();
                    map.put(c, entry);
                    tmpMap.put(c.getSlot(), map);
                }
            });

            return tmpMap.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .map(e -> new WeightItems<>(e.getKey(), e.getValue().entrySet().stream()
                            .sorted(Comparator.comparingInt(e2 -> e2.getKey().getWeight()))
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toList()))
                    )
                    .collect(Collectors.toList());
        }

    }

}
