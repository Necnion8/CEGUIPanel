package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IconEditPanel extends GUIPanel {
    private final Consumer<Optional<ItemStack>> done;
    private final ItemStack model;
    private ItemStack change;
    private PanelItem overridable;
    private boolean cloneMeta;
    private ItemFlagEditor itemFlagEditor;

    public IconEditPanel(Player player, ItemStack model, @Nullable ItemStack change, Consumer<Optional<ItemStack>> done) {
        super(player, ChatColor.DARK_GREEN + "アイコン設定", GUISize.CHEST9X3, null);
        this.done = done;
        this.model = model;
        this.change = change;
//        this.itemStack = itemStack.clone();
        this.itemFlagEditor = new ItemFlagEditor(Optional.ofNullable(change)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getItemFlags)
                .orElseGet(Sets::newHashSet)
        );
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[getSize()];

        slots[10] = overridable = new PanelItem(null)
                .setItemBuilder((p) -> buildItemStack())
                .setClickListener((e, p) -> {
                    if (!InventoryAction.SWAP_WITH_CURSOR.equals(e.getAction()))
                        return;

                    ItemStack cursor = e.getCursor();
                    if (cursor == null)
                        return;
                    change = cursor.clone();
                    this.update();
                    e.setCursor(null);
                    p.getInventory().addItem(cursor);
                });

        slots[12] = new PanelItem(null)
                .setItemBuilder((p) -> PanelItem.createItem(
                        (cloneMeta) ? Material.GOLD_NUGGET : Material.IRON_NUGGET,
                        (cloneMeta) ? ChatColor.GOLD + "メタをコピーする" : ChatColor.GRAY + "メタをコピーしない"
                ).getItemStack())
                .setClickListener((e, p) -> {
                    cloneMeta = !cloneMeta;
                    this.update();
                });

        slots[13] = new PanelItem(null)
                .setItemBuilder((p) -> {
                    List<String> lines = Lists.newArrayList(
                            ChatColor.GRAY + "左クリック: 有効性の切り替え",
                            ChatColor.GRAY + "右クリック: 次の属性を選択",
                            ""
                    );
                    lines.addAll(itemFlagEditor.buildLoreLines());
                    return PanelItem.createItem(Material.SUGAR, ChatColor.GOLD + "HideFlagsの設定", lines).getItemStack();
                })
                .setClickListener((e, p) -> {
                    if (ClickType.LEFT.equals(e.getClick())) {
                        itemFlagEditor.selectThis();
                    } else if (ClickType.RIGHT.equals(e.getClick())) {
                        itemFlagEditor.selectNext();
                    } else if (ClickType.SHIFT_RIGHT.equals(e.getClick())) {
                        itemFlagEditor.selectBack();
                    }
                    this.update();
                });

        slots[15] = PanelItem.createItem(Material.ANVIL, ChatColor.GOLD + "決定")
                .setClickListener((e, p) -> done.accept(Optional.of(buildItemStack())));

        slots[16] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                .setClickListener((e, p) -> done.accept(Optional.empty()));

        return slots;
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (InventoryAction.SWAP_WITH_CURSOR.equals(event.getAction())) {
            PanelItem item = selectPanelItem(event.getSlot());
            if (overridable.equals(item)) {
                item.getClickListener().click(event, getPlayer());
                return true;
            }
        }
        return super.onClick(event);
    }


    private ItemStack buildItemStack() {
        ItemStack itemStack;

        if (change == null) {
            itemStack = model.clone();
        } else if (!cloneMeta) {
            itemStack = model.clone();
            itemStack.setType(change.getType());
        } else {
            itemStack = change.clone();
        }
        itemStack.setAmount(1);

        Optional.ofNullable(itemStack.getItemMeta()).ifPresent(meta -> {
            meta.removeItemFlags(ItemFlag.values());
            meta.addItemFlags(itemFlagEditor.itemFlags.toArray(new ItemFlag[0]));
            itemStack.setItemMeta(meta);
        });

        return itemStack;
    }


    public static class ItemFlagEditor {
        private final Set<ItemFlag> itemFlags;
        private int cursor = 0;
        private final List<Entry> entryList;

        public ItemFlagEditor(Set<ItemFlag> flags) {
            this.itemFlags = Sets.newHashSet(flags);
            this.entryList = Lists.newArrayList(
                    new Entry("デフォルト", flags, true),
                    new Entry("全て", Stream.of(ItemFlag.values()).filter(e -> e.name().startsWith("HIDE_")).collect(Collectors.toSet()), false)
            );

            for (ItemFlag flag : ItemFlag.values()) {
                entryList.add(new Entry(Entry.getLocalizedFlagName(flag), Sets.newHashSet(flag), false));
            }
        }

        public List<String> buildLoreLines() {
            List<String> lines = Lists.newArrayList();
            for (Entry entry : entryList) {
                Entry selected = entryList.get(cursor);
                ChatColor color = (entry.equals(selected)) ? ChatColor.GOLD : ChatColor.GRAY;

                boolean equals = (entry.replace) ? Arrays.stream(ItemFlag.values())
                        .filter(flag -> entry.flags.contains(flag) == itemFlags.contains(flag))
                        .count() == itemFlags.size() : entry.flags.stream().filter(itemFlags::contains).count() >= entry.flags.size();

                String nameColor = equals ? "" + ChatColor.YELLOW : "" + ChatColor.GRAY + ChatColor.ITALIC;
                lines.add(color + "> " + nameColor + entry.display);
            }
            return lines;
        }

        public void selectNext() {
            if (cursor+1 >= entryList.size()) {
                cursor = 0;
            } else {
                cursor++;
            }
        }

        public void selectBack() {
            if (cursor-1 < 0) {
                cursor = entryList.size() - 1;
            } else {
                cursor--;
            }
        }

        public void selectThis() {
            Entry entry = entryList.get(cursor);

            if (entry.replace)
                itemFlags.clear();

            long contains = itemFlags.stream().filter(entry.flags::contains).count();
            if (contains == entry.flags.size()) {
                itemFlags.removeAll(entry.flags);
            } else  {
                itemFlags.addAll(entry.flags);
            }
        }


        public static class Entry {
            private final String display;
            private final Set<ItemFlag> flags;
            private final boolean replace;

            public Entry(String display, Set<ItemFlag> flags, boolean replace) {
                this.display = display;
                this.flags = flags;
                this.replace = replace;
            }

            public static String getLocalizedFlagName(ItemFlag flag) {
                switch (flag) {
                    case HIDE_DESTROYS:
                        return "破壊可能";
                    case HIDE_PLACED_ON:
                        return "配置可能";
                    case HIDE_ENCHANTS:
                        return "エンチャント";
                    case HIDE_ATTRIBUTES:
                        return "属性";
                    case HIDE_UNBREAKABLE:
                        return "不可壊";
                    case HIDE_POTION_EFFECTS:
                        return "ポーション効果";
                }
                return flag.name();
            }

        }
    }
}
