package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemHasCondition implements ClickCondition {

    private final @Nullable ItemStack itemStack;

    public ItemHasCondition(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public @Nullable ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean check(GUIPanel panel, Player player) {
        if (itemStack == null)
            return false;
        int count = itemStack.getAmount();
        PlayerInventory inv = player.getInventory();
        for (ItemStack next : inv) {
            if (itemStack.isSimilar(next)) {
                count -= next.getAmount();
                if (count <= 0)
                    return true;
            }
        }
        return false;
    }


    public static class Creator implements ClickConditionCreator<ItemHasCondition> {

        @Override
        public @NotNull ItemHasCondition create() {
            return new ItemHasCondition(null);
        }

        @Override
        public @Nullable ItemHasCondition createFromConfig(Map<?, ?> config) {
            Object itemStackEntry = config.get("itemstack");
            if (itemStackEntry instanceof Map) {
                //noinspection unchecked
                ItemStack itemStack = ItemStack.deserialize((Map<String, Object>) itemStackEntry);
                return new ItemHasCondition(itemStack);
            }
            return null;
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.BRICK, ChatColor.GOLD + "アイテム所持").getItemStack();
        }

        @Override
        public @NotNull String getConditionId() {
            return "hasitem";
        }
    }
}
