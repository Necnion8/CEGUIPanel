package com.gmail.necnionch.myplugin.ceguipanel.bukkit;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.MainConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.PanelConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.CustomPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.*;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.*;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class GUIPanelManager {

    private final GUIPanelPlugin plugin;
    private final MainConfig mainConfig;

    private final Map<String, ClickActionCreator<?>> actionCreatorOfId = Maps.newLinkedHashMap();
    private final Map<Class<ClickAction>, ClickActionCreator<?>> actionCreatorOfClass = Maps.newHashMap();
    private final Map<String, ConditionCreator<?>> conditionCreatorOfId = Maps.newLinkedHashMap();
    private final Map<Class<Condition>, ConditionCreator<?>> conditionCreatorOfClass = Maps.newHashMap();
    private final ClickActionCreator<NoneAction> defaultActionCreator;

    public GUIPanelManager(GUIPanelPlugin plugin, MainConfig mainConfig) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;
        Panel.OWNER = plugin;

        this.defaultActionCreator = new NoneAction.Creator();
        registerClickActionCreator(defaultActionCreator, NoneAction.class);
        registerClickActionCreator(new OpenAction.Creator(), OpenAction.class);
        registerClickActionCreator(new CloseAction.Creator(), CloseAction.class);
        registerClickActionCreator(new FunctionAction.Creator(), FunctionAction.class);
        registerClickActionCreator(new ItemGiveAction.Creator(), ItemGiveAction.class);

        registerClickConditionCreator(new ItemHasCondition.Creator(), ItemHasCondition.class);
        registerClickConditionCreator(new ItemRemoveCondition.Creator(), ItemRemoveCondition.class);
        registerClickConditionCreator(new ScoreboardCondition.Creator(), ScoreboardCondition.class);
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }


    @SuppressWarnings("unchecked")
    public <A extends ClickAction> boolean registerClickActionCreator(ClickActionCreator<A> creator, Class<A> actionClass) {
        actionCreatorOfId.put(creator.getActionId(), creator);
        actionCreatorOfClass.put((Class<ClickAction>) actionClass, creator);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <C extends Condition> boolean registerClickConditionCreator(ConditionCreator<C> creator, Class<C> conditionClass) {
        conditionCreatorOfId.put(creator.getConditionId(), creator);
        conditionCreatorOfClass.put((Class<Condition>) conditionClass, creator);
        return true;
    }

    public void unregisterClickActionCreator(ClickActionCreator<ClickAction> creator) {
        actionCreatorOfId.remove(creator.getActionId(), creator);
        actionCreatorOfClass.entrySet().removeIf(e -> creator.equals(e.getValue()));
    }

    public void unregisterClickConditionCreator(ConditionCreator<Condition> creator) {
        conditionCreatorOfId.remove(creator.getConditionId(), creator);
        conditionCreatorOfClass.entrySet().removeIf(e -> creator.equals(e.getValue()));
    }

    public @Nullable <A extends ClickAction> A createClickAction(Class<A> actionClass) {
        if (!actionCreatorOfClass.containsKey(actionClass))
            return null;
        //noinspection unchecked
        return (A) actionCreatorOfClass.get(actionClass).create();
    }

    public @Nullable <A extends ClickAction> A createClickActionFromConfig(Class<A> actionClass, Map<?, ?> config) {
        if (!actionCreatorOfClass.containsKey(actionClass))
            return null;
        //noinspection unchecked
        return (A) actionCreatorOfClass.get(actionClass).createFromConfig(config);
    }

    public @Nullable <C extends Condition> C createClickCondition(Class<C> conditionClass) {
        if (!conditionCreatorOfClass.containsKey(conditionClass))
            return null;
        //noinspection unchecked
        return (C) conditionCreatorOfClass.get(conditionClass).create();
    }

    public @Nullable <C extends Condition> C createClickConditionFromConfig(Class<C> conditionClass, Map<?, ?> config) {
        if (!conditionCreatorOfClass.containsKey(conditionClass))
            return null;
        //noinspection unchecked
        return (C) conditionCreatorOfClass.get(conditionClass).createFromConfig(config);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <A extends ClickAction> ClickActionCreator<A> getActionCreator(Class<A> actionClass) {
        return (ClickActionCreator<A>) actionCreatorOfClass.get(actionClass);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <C extends Condition> ConditionCreator<C> getConditionCreator(Class<C> conditionClass) {
        return (ConditionCreator<C>) conditionCreatorOfClass.get(conditionClass);
    }


    public Map<String, ClickActionCreator<?>> getActionCreators() {
        return Collections.unmodifiableMap(actionCreatorOfId);
    }

    public Map<String, ConditionCreator<?>> getConditionCreators() {
        return Collections.unmodifiableMap(conditionCreatorOfId);
    }

    public ClickActionCreator<NoneAction> getDefaultActionCreator() {
        return defaultActionCreator;
    }

    public ItemConfig cloneItemConfig(ItemConfig itemConfig, int weight) {
        Map<String, Object> data = Maps.newHashMap();
        itemConfig.getClickAction().serialize(data);
        ClickAction clickAction = createClickActionFromConfig(itemConfig.getClickAction().getClass(), data);

        data.clear();
        Condition clickCondition = Optional.ofNullable(itemConfig.getClickCondition())
                .map(cond -> {
                    cond.serialize(data);
                    return createClickConditionFromConfig(cond.getClass(), data);
                })
                .orElse(null);

        data.clear();
        Condition viewCondition = Optional.ofNullable(itemConfig.getViewCondition())
                .map(cond -> {
                    cond.serialize(data);
                    return createClickConditionFromConfig(cond.getClass(), data);
                })
                .orElse(null);

        GUIIcon icon = itemConfig.getIcon().copy();

        return new ItemConfig(itemConfig.getSlot(), clickAction, clickCondition, viewCondition, icon, weight);
    }

    public PanelConfig clonePanelConfig(PanelConfig panelConfig) {
        Map<String, Object> data = panelConfig.serialize(this);
        return PanelConfig.deserialize(this, panelConfig.getName(), data);
    }

    public PanelConfig clonePanelConfig(PanelConfig panelConfig, String newName) {
        Map<String, Object> data = panelConfig.serialize(this);
        return PanelConfig.deserialize(this, newName, data);
    }


    public void savePanelConfig(PanelConfig panelConfig) {
        mainConfig.removePanelConfigByName(panelConfig.getName());
        mainConfig.addPanelConfigByName(panelConfig.getName(), panelConfig);
        mainConfig.save();
    }

    public @Nullable PanelConfig removePanelConfigByName(String panelName) {
        PanelConfig pc = mainConfig.removePanelConfigByName(panelName);
        mainConfig.save();
        return pc;
    }

    public void removePanelConfig(PanelConfig panelConfig) {
        PanelConfig pc = mainConfig.getPanelConfigByName(panelConfig.getName());
        if (panelConfig.equals(pc)) {
            mainConfig.removePanelConfigByName(pc.getName());
        }
    }

    public void addPanelConfig(PanelConfig panelConfig) {
        mainConfig.addPanelConfigByName(panelConfig.getName(), panelConfig);
        mainConfig.save();
    }

    public @Nullable PanelConfig getPanelConfigByName(String name) {
        return mainConfig.getPanelConfigByName(name);
    }

    public Set<String> getPanelNames() {
        return mainConfig.getPanelNames();
    }

    public @Nullable GUIPanel createGUIPanelByName(String name, Player player) {
        PanelConfig panelConfig = mainConfig.getPanelConfigByName(name);
        if (panelConfig == null)
            return null;

        return CustomPanel.create(player, clonePanelConfig(panelConfig));
    }

    public GUIPanel createGUIPanel(PanelConfig panelConfig, Player player) {
        return CustomPanel.create(player, clonePanelConfig(panelConfig));
    }


}
