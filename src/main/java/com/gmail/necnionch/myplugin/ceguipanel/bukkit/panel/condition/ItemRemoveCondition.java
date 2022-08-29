package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemRemoveCondition implements Condition {

    private @Nullable ItemStack itemStack;

    public ItemRemoveCondition(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public @Nullable ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
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

    @Override
    public void complete(GUIPanel panel, Player player) {
        if (itemStack == null)
            return;

        int count = itemStack.getAmount();
        PlayerInventory inv = player.getInventory();

        for (ItemStack next : inv) {
            if (itemStack.isSimilar(next)) {
                if (count >= next.getAmount()) {  // todo test
                    count -= next.getAmount();
                    next.setAmount(0);
                } else {
                    next.setAmount(next.getAmount() - count);
                    count = 0;
                }
                if (count <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public @Nullable List<String> getDescription(Player player) {
        if (itemStack == null)
            return null;

        List<String> lore = Lists.newArrayList();
        lore.add("アイテムの削除");

        String itemName = Optional.ofNullable(itemStack.getItemMeta())
                .filter(ItemMeta::hasDisplayName)
                .map(ItemMeta::getDisplayName)
                .orElse(itemStack.getType().name());
        String countName = " [x" + itemStack.getAmount() + "]";
        lore.add(ChatColor.GRAY + "アイテム: " + ChatColor.WHITE + itemName + ChatColor.GRAY + countName);
        return lore;
    }

    @Override
    public void serialize(Map<String, Object> config) {
        config.put("itemstack", Optional.ofNullable(itemStack).map(ItemStack::clone).orElse(null));
    }

    @Override
    public void openSettingGUI(Player player, Panel parent, ItemConfig itemConfig, Runnable done) {
        new Configurator(player, itemConfig, done).open(parent);
    }


    public class Configurator extends Panel {
        private final Runnable done;
        private final ItemConfig itemConfig;
        private PanelItem overridable;

        public Configurator(Player player, ItemConfig itemConfig, Runnable done) {
            super(player, 27, ChatColor.DARK_AQUA + "削除アイテムを設定");
            this.done = done;
            this.itemConfig = itemConfig;
        }

        @Override
        public PanelItem[] build() {
            PanelItem[] slots = new PanelItem[getSize()];

            slots[10] = overridable = new PanelItem(null)
                    .setItemBuilder((p) -> Optional.ofNullable(itemStack)
                            .map(ItemStack::clone)
                            .map(item -> {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    List<String> lore = Lists.newArrayList(
                                            ChatColor.GRAY + "個数: " + ChatColor.GOLD + itemStack.getAmount(),
                                            "",
                                            ChatColor.GRAY + "左クリックで個数を増やし、右クリックで減らす。",
                                            ChatColor.GRAY + "シフト押しで8個ずつ変更する");
                                    if (meta.getLore() != null) {
                                        lore.add(ChatColor.DARK_GRAY + "================================");
                                        lore.addAll(meta.getLore());
                                    }
                                    meta.setLore(lore);
                                    item.setItemMeta(meta);
                                }
                                return item;
                            })
                            .orElseGet(() -> PanelItem.createItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + "ここにアイテムを配置").getItemStack()))
                    .setClickListener((e, p) -> {
                        if (InventoryAction.SWAP_WITH_CURSOR.equals(e.getAction())) {
                            // new item
                            ItemStack cursor = e.getCursor();
                            if (cursor != null) {
                                itemStack = cursor.clone();
                                this.update();
                                e.setCursor(null);
                                p.getInventory().addItem(cursor);
                            }

                        } else if (ClickType.LEFT.equals(e.getClick())) {
                            // +amount
                            if (itemStack != null) {
                                int amount = Math.min(itemStack.getAmount(), itemStack.getMaxStackSize());
                                itemStack.setAmount(Math.min(amount+1, itemStack.getMaxStackSize()));
                                this.update();
                            }
                        } else if (ClickType.SHIFT_LEFT.equals(e.getClick())) {
                            // +amount 8
                            if (itemStack != null) {
                                int amount = Math.min(itemStack.getAmount(), itemStack.getMaxStackSize());
                                itemStack.setAmount(Math.min(amount+8, itemStack.getMaxStackSize()));
                                this.update();
                            }
                        } else if (ClickType.RIGHT.equals(e.getClick())) {
                            // -amount
                            if (itemStack != null) {
                                int amount = Math.min(itemStack.getAmount(), itemStack.getMaxStackSize());
                                itemStack.setAmount(Math.max(1, amount-1));
                                this.update();
                            }
                        } else if (ClickType.SHIFT_RIGHT.equals(e.getClick())) {
                            // -amount 8
                            if (itemStack != null) {
                                int amount = Math.min(itemStack.getAmount(), itemStack.getMaxStackSize());
                                itemStack.setAmount(Math.max(1, amount-8));
                                this.update();
                            }
                        }
                    });

            slots[12] = new PanelItem(null)
                    .setItemBuilder((p) -> {
                        Material material = Material.IRON_NUGGET;
                        String color = ChatColor.GRAY.toString() + ChatColor.ITALIC;;
                        String color2 = color;
                        if (itemStack != null) {
                            if (!itemStack.getType().equals(itemConfig.getIcon().getItemStack().getType())) {
                                material = Material.GOLD_NUGGET;
                                color = ChatColor.GOLD.toString();
                            }
                            if (!itemStack.isSimilar(itemConfig.getIcon().getItemStack())) {
                                color2 = ChatColor.YELLOW.toString();
                            }
                        }
                        return PanelItem.createItem(material, color + "アイコンに設定", Lists.newArrayList(
                                color2 + "シフト押しでメタもコピー"
                        )).getItemStack();
                    })
                    .setClickListener((e, p) -> {
                        if (itemStack != null) {
                            if (ClickType.LEFT.equals(e.getClick())) {
                                if (!itemStack.getType().equals(itemConfig.getIcon().getItemStack().getType())) {
                                    itemConfig.getIcon().getItemStack().setType(itemStack.getType());
                                    playClickSound(p, Sound.BLOCK_LEVER_CLICK, 0);
                                    this.update();
                                }
                            } else if (ClickType.SHIFT_LEFT.equals(e.getClick())) {
                                if (!itemStack.isSimilar(itemConfig.getIcon().getItemStack())) {
                                    GUIIcon icon = itemConfig.getIcon();
                                    icon.setItemStack(itemStack.clone());
                                    icon.getItemStack().setAmount(1);
                                    playClickSound(p, Sound.BLOCK_LEVER_CLICK, 0);
                                    this.update();
                                }
                            }
                        }
                    });

            slots[16] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                    .setClickListener((e, p) -> done.run());
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

    }


    public static class Creator implements ConditionCreator<ItemRemoveCondition> {

        @Override
        public @NotNull ItemRemoveCondition create() {
            return new ItemRemoveCondition(null);
        }

        @Override
        public @Nullable ItemRemoveCondition createFromConfig(Map<?, ?> config) {
            ItemStack itemStack = (ItemStack) config.get("itemstack");
            return (itemStack != null) ? new ItemRemoveCondition(itemStack.clone()) : null;
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.BRICK, ChatColor.GOLD + "アイテム所持+削除").getItemStack();
        }

        @Override
        public @NotNull String getConditionId() {
            return "removeitem";
        }
    }
}
