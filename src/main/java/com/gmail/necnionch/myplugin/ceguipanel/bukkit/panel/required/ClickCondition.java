package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import org.bukkit.entity.Player;

public interface ClickCondition {

    boolean check(GUIPanel panel, Player player);

}
