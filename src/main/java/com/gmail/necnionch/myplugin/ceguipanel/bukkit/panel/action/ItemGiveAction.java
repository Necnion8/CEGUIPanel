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
import java.util.stream.Collectors;

public class ItemGiveAction implements ClickAction {

    private @Nullable ItemStack itemStack;

    public ItemGiveAction(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public @Nullable ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean action(GUIPanel panel, Player player) {
        if (itemStack != null && !itemStack.getType().isAir()) {
            player.getInventory().addItem(itemStack.clone()).forEach((i, itemStack) ->
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack)
            );
            return true;
        }
        return false;
    }


    public static class Creator implements ClickActionCreator<ItemGiveAction> {

        @Override
        public @NotNull ItemGiveAction create() {
            return new ItemGiveAction(null);
        }

        @Override
        public @Nullable ItemGiveAction createFromConfig(Map<?, ?> config) {
            ItemStack itemStack = null;
            if (config.get("itemstack") instanceof Map) {
                Map<String, Object> serialized = ((Map<?, ?>) config.get("itemstack"))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getKey() instanceof String)
                        .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));
                try {
                    itemStack = ItemStack.deserialize(serialized);
                } catch (Exception e) {
                    e.printStackTrace();  // TODO: logging
                }
            }
            return new ItemGiveAction(itemStack);
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.GOLD_NUGGET, ChatColor.GOLD + "アイテムを与える").getItemStack();
        }

        @Override
        public @NotNull String getActionId() {
            return "giveitem";
        }
    }
}
