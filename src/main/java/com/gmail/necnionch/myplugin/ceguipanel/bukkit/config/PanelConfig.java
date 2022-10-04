package com.gmail.necnionch.myplugin.ceguipanel.bukkit.config;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickActionCreator;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.Condition;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.ConditionCreator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PanelConfig {
    private @NotNull final String name;
    private @NotNull String title;
    private @NotNull GUISize size;
    private final List<ItemConfig> items;

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

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public void setSize(@NotNull GUISize size) {
        this.size = size;
    }

    public List<ItemConfig> items() {
        return items;
    }


    public Map<String, Object> serialize(GUIPanelManager mgr) {
        Map<String, Object> data = Maps.newHashMap();

        data.put("display", title);
        data.put("size", size.name().toLowerCase(Locale.ROOT));

        List<Map<String, Object>> slots = Lists.newArrayList();
        data.put("slots", slots);

        for (ItemConfig itemConfig : items) {
            Map<String, Object> itemData = Maps.newHashMap();
            itemData.put("slot", itemConfig.getSlot());
            if (itemConfig.getWeight() != 1)
                itemData.put("weight", itemConfig.getWeight());

            String actionName = mgr.getActionCreator(itemConfig.getClickAction().getClass()).getActionId();
            itemData.put("action", actionName);
            itemConfig.getClickAction().serialize(itemData);

            Optional.ofNullable(itemConfig.getClickCondition()).ifPresent((cond -> {
                Map<String, Object> condData = Maps.newHashMap();

                String condName = mgr.getConditionCreator(cond.getClass()).getConditionId();
                condData.put("type", condName);
                cond.serialize(condData);

                itemData.put("click_condition", condData);
            }));

            Optional.ofNullable(itemConfig.getViewCondition()).ifPresent((cond -> {
                Map<String, Object> condData = Maps.newHashMap();

                String condName = mgr.getConditionCreator(cond.getClass()).getConditionId();
                condData.put("type", condName);
                cond.serialize(condData);

                itemData.put("view_condition", condData);
            }));

            itemData.put("icon", itemConfig.getIcon().serialize());

            slots.add(itemData);
        }
        return data;
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

            Map<?, ?> slotEntry = (Map<?, ?>) eObject;
//            Map<String, Object> slotEntry = ((Map<?, ?>) eObject).entrySet().stream()
//                    .filter(e -> e.getKey() instanceof String)
//                    .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));

            int slot = (Integer) slotEntry.get("slot");
            int weight = 1;
            if (slotEntry.get("weight") instanceof Integer) {
                weight = (Integer) slotEntry.get("weight");
            }

            String actionName = (String) slotEntry.get("action");
            ClickAction clickAction = null;
            ClickActionCreator<?> actionCreator = mgr.getActionCreators().get(actionName);
            if (actionCreator != null)
                clickAction = actionCreator.createFromConfig(slotEntry);
            if (clickAction == null)
                throw new IllegalStateException("Failed to create ClickAction: " + actionName);

            Condition clickCondition = null;
            if (slotEntry.get("click_condition") instanceof Map) {
                Map<?, ?> requiredEntry = (Map<?, ?>) slotEntry.get("click_condition");
                String conditionName = (String) requiredEntry.get("type");
                ConditionCreator<?> conditionCreator = mgr.getConditionCreators().get(conditionName);
                if (conditionCreator != null)
                    clickCondition = conditionCreator.createFromConfig(requiredEntry);
                if (clickCondition == null) {
                    mgr.getLogger().severe("Failed to create click condition: " + conditionName);
                    continue;
                }
            }

            Condition viewCondition = null;
            if (slotEntry.get("view_condition") instanceof Map) {
                Map<?, ?> requiredEntry = (Map<?, ?>) slotEntry.get("view_condition");
                String conditionName = (String) requiredEntry.get("type");
                ConditionCreator<?> conditionCreator = mgr.getConditionCreators().get(conditionName);
                if (conditionCreator != null)
                    viewCondition = conditionCreator.createFromConfig(requiredEntry);
                if (viewCondition == null) {
                    mgr.getLogger().severe("Failed to create view condition: " + conditionName);
                    continue;
                }
            }

            GUIIcon icon = null;
            if (slotEntry.get("icon") instanceof Map) {
                try {
                    icon = GUIIcon.deserialize((Map<?, ?>) slotEntry.get("icon"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (icon == null) {
                mgr.getLogger().severe("Icon has not set: slot " + slot);
                continue;
            }
            items.add(new ItemConfig(slot, clickAction, clickCondition, viewCondition, icon, weight));
        }

        return new PanelConfig(name, displayName, size, items);
    }

}
