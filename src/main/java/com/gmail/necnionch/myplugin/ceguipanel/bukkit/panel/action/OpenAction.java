package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class OpenAction implements ClickAction {

    private @Nullable String panel;

    public OpenAction(@Nullable String panel) {
        this.panel = panel;
    }

    public @Nullable String getPanelName() {
        return panel;
    }

    public void setPanelName(@Nullable String panel) {
        this.panel = panel;
    }

    public @Nullable GUIPanel getPanel(Player player) {
        if (panel != null) {
            GUIPanelManager mgr = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(GUIPanelManager.class)).getProvider();
            return mgr.createGUIPanelByName(panel, player);
        }
        return null;
    }

    @Override
    public boolean action(GUIPanel panel, Player player) {
        GUIPanel nextPanel = getPanel(player);
        if (nextPanel != null) {
            panel.playClickSound(Sound.BLOCK_LEVER_CLICK, 2);
            nextPanel.open(panel);
            return true;
        }
        JavaPlugin.getPlugin(GUIPanelPlugin.class).getLogger().warning("Failed to open gui: " + panel);
        return false;
    }

    @Override
    public List<String> getDescription(Player player) {
        return Lists.newArrayList(
                "パネルを開く",
                ChatColor.GRAY + "パネル: " + ChatColor.WHITE + (panel != null ? panel : "？")
        );
    }

    @Override
    public void serialize(Map<String, Object> config) {
        config.put("panel", panel);
    }

    @Override
    public void openSettingGUI(Player player, Panel parent, ItemConfig itemConfig, Runnable done) {
        new Configurator(player, done).open(parent);
    }


    public class Configurator extends Panel {
        private final Runnable done;

        public Configurator(Player player, Runnable done) {
            super(player, 27, ChatColor.DARK_AQUA + "パネルを開く");
            this.done = done;
        }

        @Override
        public PanelItem[] build() {
            PanelItem[] slots = new PanelItem[getSize()];

            slots[10] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(Material.CHEST, ChatColor.GOLD + "パネル名を入力", Lists.newArrayList(
                            ChatColor.GRAY + "パネル: " + Optional.ofNullable(panel).orElse("なし")
                    )).getItemStack())
                    .setClickListener((e, p) -> {
                        new AnvilGUI.Builder()
                                .plugin(Panel.OWNER)
                                .title("パネル名を入力")
                                .text(Optional.ofNullable(panel).orElse(""))
                                .onComplete((p1, s) -> {
                                    panel = s;

                                    Bukkit.getScheduler().runTaskLater(Panel.OWNER, (@Nullable Runnable) this::open, 2);
                                    return AnvilGUI.Response.text("");
                                })
                                .open(p);
                    });

            slots[16] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                    .setClickListener((e, p) -> done.run());
            return slots;
        }
    }


    public static class Creator implements ClickActionCreator<OpenAction> {

        @Override
        public @NotNull OpenAction create() {
            return new OpenAction(null);
        }

        @Override
        public @Nullable OpenAction createFromConfig(Map<?, ?> config) {
            Object panel = config.get("panel");
            return new OpenAction(panel instanceof String ? (String) panel : null);
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.CHEST, ChatColor.GOLD + "パネルを開く").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "open";
        }
    }

}
