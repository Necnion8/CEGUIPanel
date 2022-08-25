package com.gmail.necnionch.myplugin.ceguipanel.bukkit.config;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required.ClickCondition;
import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PanelConfig {
    private @NotNull
    final String name;
    private final @NotNull GUISize size;
    private final List<ItemConfig> items;
    private @NotNull final String title;

    public PanelConfig(@NotNull String name, @NotNull String title, @NotNull GUISize size, List<ItemConfig> items) {
        this.name = name;
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull GUISize getSize() {
        return size;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public List<ItemConfig> getItems() {
        return items;
    }

    public static PanelConfig deserialize(GUIPanelManager mgr, String name, Map<String, Object> data) {
        String displayName = (String) data.get("display");
        String sizeName = (String) data.get("size");
        GUISize size;
        try {
            size = GUISize.valueOf(sizeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            size = GUISize.CHEST9X6;
        }

        List<ItemConfig> items = Lists.newArrayList();
        List<?> slots = (List<?>) data.get("slots");
        for (Object eObject : slots) {
            if (!(eObject instanceof Map))
                continue;

            Map<String, Object> slotEntry = ((Map<?, ?>) eObject).entrySet().stream()
                    .filter(e -> e.getKey() instanceof String)
                    .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));

            int slot = (int) slotEntry.get("slot");

            String actionName = (String) slotEntry.get("action");
            Class<ClickAction> clickActionClass = mgr.getActionClasses().get(actionName);
            ClickAction clickAction = mgr.createClickActionFromConfig(clickActionClass, slotEntry);
            if (clickAction == null)
                throw new IllegalStateException("Failed to create ClickAction: " + actionName);

            ClickCondition clickCondition = null;
            if (slotEntry.get("required") instanceof Map) {
                String conditionName = (String) slotEntry.get("required");
                Class<ClickCondition> clickConditionClass = mgr.getConditionClasses().get(conditionName);
                clickCondition = mgr.createClickConditionFromConfig(clickConditionClass, ((Map<?, ?>) slotEntry.get("required")));
            }

            GUIIcon icon = null;
            if (slotEntry.get("icon") instanceof Map) {
                Map<String, ?> iconEntry = ((Map<?, ?>) slotEntry.get("icon")).entrySet().stream()
                        .filter(e -> e.getKey() instanceof String)
                        .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));

                if (iconEntry.get("itemstack") instanceof Map) {
                    //noinspection unchecked
                    ItemStack itemStack = ItemStack.deserialize(((Map<String, Object>) iconEntry.get("itemstack")));
                    String display = (String) iconEntry.get("display");
                    List<String> lores = Collections.emptyList();
                    if (iconEntry.get("lores") instanceof List) {
                        lores = ((List<?>) iconEntry.get("lores")).stream()
                                .filter(e -> e instanceof String)
                                .map(e -> (String) e)
                                .collect(Collectors.toList());
                    }

                    icon = new GUIIcon(display, lores, itemStack);
                }
            }
            items.add(new ItemConfig(slot, clickAction, clickCondition, icon));
        }

        return new PanelConfig(name, displayName, size, items);
    }

}
