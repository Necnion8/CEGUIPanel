package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.Panel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Condition {

    boolean check(GUIPanel panel, Player player);

    default void complete(GUIPanel panel, Player player) {}

    @Nullable List<String> getDescription(Player player);

    default void openSettingGUI(Player player, Panel parent, ItemConfig itemConfig, Runnable done) {
        done.run();
    }

    void serialize(Map<String, Object> config);

}
