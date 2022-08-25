package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FunctionAction implements ClickAction {

    private @Nullable String functionName;
    private boolean keepOpen;

    public FunctionAction(@Nullable String function, boolean keepOpen) {
        functionName = function;
        this.keepOpen = keepOpen;
    }

    public @Nullable String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(@Nullable String functionName) {
        this.functionName = functionName;
    }

    public boolean isKeepOpen() {
        return keepOpen;
    }

    public void setKeepOpen(boolean keepOpen) {
        this.keepOpen = keepOpen;
    }

    @Override
    public boolean action(GUIPanel panel, Player player) {
        // TODO: execute function
        return true;
    }

//    @Override
//    public String getName() {
//        return "Functionを実行する";
//    }


    public static class Creator implements ClickActionCreator<FunctionAction> {

        @Override
        public @NotNull FunctionAction create() {
            return new FunctionAction(null, false);
        }

        @Override
        public @Nullable FunctionAction createFromConfig(Map<?, ?> config) {
            String function;
            try {
                function = ((String) config.get("function"));
            } catch (ClassCastException e) {
                function = null;
            }
            boolean keepOpen = false;
            try {
                if (config.containsKey("keep_open"))
                    keepOpen = (Boolean) config.get("keep_open");
            } catch (ClassCastException ignored) {
            }
            return new FunctionAction(function, keepOpen);
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.REPEATING_COMMAND_BLOCK, ChatColor.GOLD + "Functionを実行する").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "function";
        }
    }

}
