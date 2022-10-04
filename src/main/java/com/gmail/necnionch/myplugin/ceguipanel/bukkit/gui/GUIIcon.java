package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms.NMSHandler;
import com.google.common.collect.Maps;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GUIIcon {

    private @NotNull ItemStack itemStack;
    private @Nullable String displayName;
    private @Nullable String[] loreLines;

    public GUIIcon(@NotNull ItemStack itemStack, @Nullable String displayName, String[] loreLines) {
        this.itemStack = itemStack;
        this.displayName = displayName;
        this.loreLines = loreLines;
    }

    public GUIIcon(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setItemStack(@NotNull ItemStack itemStack, boolean saveNames) {
        this.itemStack = itemStack;
        if (saveNames) {
            Optional.ofNullable(itemStack.getItemMeta()).ifPresent(meta -> {
                this.displayName = meta.getDisplayName();
                this.loreLines = Optional.ofNullable(meta.getLore())
                        .filter(Predicate.not(List::isEmpty))
                        .map(lore -> lore.toArray(new String[0]))
                        .orElseGet(() -> new String[0]);
            });
        } else {
            this.displayName = null;
            this.loreLines = null;
        }
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        setItemStack(itemStack, true);
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @Nullable String getDisplayName() {
        return displayName;
    }

    public String[] getLoreLines() {
        return Optional.ofNullable(loreLines).orElseGet(() -> new String[0]);
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    public void setLoreLines(@Nullable String[] loreLines) {
        if (loreLines.length <= 0)
            loreLines = null;
        this.loreLines = Optional.ofNullable(loreLines).orElseGet(() -> new String[0]);
    }

    public @NotNull ItemStack buildItemStack(Player player) {
        ItemStack itemStack = this.itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(Optional.ofNullable(displayName)
                    .map(s -> formatTellrawJson(player, s))
                    .orElse(null));
            meta.setLore(Optional.ofNullable(loreLines)
                    .map(lines -> Stream.of(lines)
                            .map(s -> formatTellrawJson(player, s))
                            .collect(Collectors.toList()))
                    .orElseGet(Collections::emptyList));

            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }


    public GUIIcon copy() {
        return new GUIIcon(itemStack.clone(), displayName, loreLines);
    }


    private String formatTellrawJson(CommandSender sender, String string) {
        try {
            return NMSHandler.getNMS().formatTellrawJson(sender, string);
        } catch (Throwable e) {
            return ChatColor.translateAlternateColorCodes('&', string);
        }
    }


    public Map<String, Object> serialize() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("itemstack", getItemStack().clone());
        data.put("displayname", displayName);
        data.put("lorelines", Optional.ofNullable(loreLines).map(Arrays::asList).orElse(null));
        return data;
    }

    public static @Nullable GUIIcon deserialize(Map<?, ?> data) {
        ItemStack itemStack = ((ItemStack) data.get("itemstack"));
        if (itemStack == null)
            return null;

        itemStack = itemStack.clone();

        String displayName = ((String) data.get("displayname"));
        String[] loreLines = Optional.ofNullable(data.get("lorelines"))
                .map(obj -> ((List<?>)obj).stream().map(o -> (String) o).toArray(String[]::new))
                .orElse(null);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            boolean changes = false;
            if (displayName == null && itemMeta.hasDisplayName()) {
                displayName = itemMeta.getDisplayName();
                itemMeta.setDisplayName(null);
                changes = true;
            }
            if (loreLines == null && itemMeta.hasLore()) {
                loreLines = Objects.requireNonNull(itemMeta.getLore()).toArray(new String[0]);
                itemMeta.setLore(null);
                changes = true;
            }
            if (changes)
                itemStack.setItemMeta(itemMeta);
        }

        return new GUIIcon(itemStack, displayName, loreLines);
    }


}
