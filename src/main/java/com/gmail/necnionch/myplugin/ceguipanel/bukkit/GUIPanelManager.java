package com.gmail.necnionch.myplugin.ceguipanel.bukkit;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.*;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required.*;
import com.google.common.collect.Maps;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class GUIPanelManager {

    private final GUIPanelPlugin plugin;
    private final Map<String, Class<ClickAction>> actionClasses = Maps.newHashMap();
    private final Map<Class<?>, ClickActionCreator<?>> actionCreatorsClass = Maps.newHashMap();
    private final Map<String, Class<?>> conditionClasses = Maps.newHashMap();
    private final Map<Class<?>, ClickConditionCreator<?>> conditionCreatorsClass = Maps.newHashMap();

    public GUIPanelManager(GUIPanelPlugin plugin) {
        this.plugin = plugin;

        registerClickActionCreator(new NoneAction.Creator(), NoneAction.class);
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


    public @Nullable GUIPanel createGUIPanelByName(String name, Player player) {
        World w;
        OpenAction clickAction = createClickAction(OpenAction.class);
//        clickAction.openPanel()
        return null;
    }


    public <A extends ClickAction> boolean registerClickActionCreator(ClickActionCreator<?> creator, Class<A> actionClass) {
        //noinspection unchecked
        actionClasses.put(creator.getActionId(), (Class<ClickAction>) actionClass);
        actionCreatorsClass.put(actionClass, creator);
        return true;
    }

    public <C extends ClickCondition> boolean registerClickConditionCreator(ClickConditionCreator<?> creator, Class<C> conditionClass) {
        conditionClasses.put(creator.getConditionId(), conditionClass);
        conditionCreatorsClass.put(conditionClass, creator);
        return true;
    }

    public @Nullable <A extends ClickAction> A createClickAction(Class<A> actionClass) {
        if (!actionCreatorsClass.containsKey(actionClass))
            return null;
        //noinspection unchecked
        return (A) actionCreatorsClass.get(actionClass).create();
    }

    public @Nullable <A extends ClickAction> A createClickActionFromConfig(Class<A> actionClass, Map<?, ?> config) {
        if (!actionCreatorsClass.containsKey(actionClass))
            return null;
        //noinspection unchecked
        return (A) actionCreatorsClass.get(actionClass).createFromConfig(config);
    }

    public <A extends ClickAction> Map<String, Class<ClickAction>> getActionClasses() {
        return Collections.unmodifiableMap(actionClasses);
    }

    public @Nullable <C extends ClickCondition> C createClickCondition(Class<C> conditionClass) {
        if (!conditionCreatorsClass.containsKey(conditionClass))
            return null;
        //noinspection unchecked
        return (C) conditionCreatorsClass.get(conditionClass);
    }

    public @Nullable <C extends ClickCondition> C createClickConditionFromConfig(Class<C> conditionClass, Map<?, ?> config) {
        if (!conditionCreatorsClass.containsKey(conditionClass))
            return null;
        //noinspection unchecked
        return (C) conditionCreatorsClass.get(conditionClass).createFromConfig(config);
    }

    public Map<String, Class<?>> getConditionClasses() {
        return Collections.unmodifiableMap(conditionClasses);
    }
    
}
