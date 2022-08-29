package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.util.ComponentUtil;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PanelItem {

    private ItemStack item;
    private ClickEventListener clickListener = (e, p) -> {};
    private ItemBuilder itemBuilder = (p) -> item;


    public PanelItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public PanelItem setClickListener(ClickEventListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public PanelItem setItemBuilder(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ClickEventListener getClickListener() {
        return clickListener;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public PanelItem clone() {
        PanelItem item = new PanelItem(this.item.clone());
        item.setClickListener(clickListener);
        item.setItemBuilder(itemBuilder);
        return item;
    }



    public interface ClickEventListener {
        void click(InventoryClickEvent event, Player player);
    }

    public interface ItemBuilder {
        ItemStack build(Player player);
    }


    public static PanelItem createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return new PanelItem(item);
    }


    public static PanelItem createItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(loreLines);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return new PanelItem(item);
    }

    public static PanelItem createBlankItem() {
        return createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString());
    }

    public static Builder create(Material material) {
        return new Builder(material);
    }


    public static class Builder {

        private final Material material;
        private String name;
        private final List<String> loreLines = Lists.newArrayList();

        private Builder(Material material) {
            this.material = material;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder name(ComponentBuilder nameBuilder) {
            this.name = ComponentUtil.componentToLegacy(nameBuilder.create());
            return this;
        }

        public Builder addLore(String line) {
            loreLines.add(line);
            return this;
        }

        public Builder addLore(ComponentBuilder lineBuilder) {
            loreLines.add(ComponentUtil.componentToLegacy(lineBuilder.create()));
            return this;
        }

        public Builder lores(List<String> loreLines) {
            this.loreLines.addAll(loreLines);
            return this;
        }

        public PanelItem build() {
            return new PanelItem(buildItemStack());
        }

        public ItemStack buildItemStack() {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(loreLines);
                meta.addItemFlags(ItemFlag.values());
                item.setItemMeta(meta);
            }
            return item;
        }
    }

}
