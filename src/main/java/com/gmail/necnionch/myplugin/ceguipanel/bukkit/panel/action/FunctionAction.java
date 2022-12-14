package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelPlugin;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.google.common.collect.Lists;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (functionName == null)
            return false;

//        String line = "execute as " + player.getUniqueId() + " at @s run function " + functionName;
//        try {
//            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line);
//        } catch (CommandException e) {
//            e.printStackTrace();
//            return false;
//        }

//        if (NMSHandler.getNMS().executeFunction(player, functionName)) {

        GUIPanelPlugin.executeFunction(player, functionName);
            if (!keepOpen) {
                panel.destroy();
                panel.playClickSound(Sound.BLOCK_NOTE_BLOCK_BASS, 2);
            } else {
                panel.playClickSound(Sound.BLOCK_LEVER_CLICK, 2);
            }
            return true;
//        }
//        return false;
    }

    @Override
    public List<String> getDescription(Player player) {
        return Lists.newArrayList(
                "???????????????????????????",
                ChatColor.GRAY + "??????: " + ChatColor.WHITE + (functionName != null ? functionName : "???")
        );
    }

    @Override
    public void serialize(Map<String, Object> config) {
        config.put("function", functionName);
        if (keepOpen)
            config.put("keep_open", true);
        else
            config.remove("keep_open");
    }

    @Override
    public void openSettingGUI(Player player, Panel parent, ItemConfig itemConfig, Runnable done) {
        new Configurator(player, done).open(parent);
    }


    public class Configurator extends Panel {
        private final Runnable done;

        public Configurator(Player player, Runnable done) {
            super(player, 27, ChatColor.RESET.toString());
            this.done = done;
        }

        @Override
        public PanelItem[] build() {
            PanelItem[] slots = new PanelItem[getSize()];

            slots[10] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(Material.ANVIL, ChatColor.GOLD + "?????????????????????????????????", Lists.newArrayList(
                            ChatColor.GRAY + "??????: " + Optional.ofNullable(functionName).orElse("??????")
                            )).getItemStack())
                    .setClickListener((e, p) -> {
                        new AnvilGUI.Builder()
                                .plugin(Panel.OWNER)
                                .title("?????????????????????????????????")
                                .text(Optional.ofNullable(functionName).orElse(""))
                                .onComplete((p1, s) -> {
                                    functionName = s;

                                    Bukkit.getScheduler().runTaskLater(Panel.OWNER, (@NotNull Runnable) this::open, 2);
                                    return AnvilGUI.Response.text("");
                                })
                                .open(p);
                    });

            slots[12] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(
                            keepOpen ? Material.LIME_DYE : Material.RED_DYE,
                            ChatColor.GOLD + "???????????????????????? " + ChatColor.GRAY + "- "
                                    + (keepOpen ? ChatColor.GREEN + "??????" : ChatColor.RED + "??????")).getItemStack())
                    .setClickListener((e, p) -> {
                        keepOpen = !keepOpen;
                        this.update();
                    });

            slots[16] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "??????")
                    .setClickListener((e, p) -> done.run());
            return slots;
        }
    }


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
            return PanelItem.createItem(Material.REPEATING_COMMAND_BLOCK, ChatColor.GOLD + "????????????????????????????????????").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "function";
        }
    }

}
