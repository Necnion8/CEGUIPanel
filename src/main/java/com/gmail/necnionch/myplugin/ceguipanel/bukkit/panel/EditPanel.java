package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.PanelConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.*;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickActionCreator;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.Condition;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.ConditionCreator;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EditPanel extends GUIPanel {

    private final List<WeightItems<ItemConfig>> items;
    private final GUIPanelManager mgr;
    private WeightItems<ItemConfig> swapping;
    private boolean changed;

    public EditPanel(GUIPanelManager mgr, Player player, @NotNull PanelConfig config) {
        super(player, config.getName() + " - " + config.getTitle(), config.getSize(), null);
        this.mgr = mgr;
        items = WeightItems.sortedOf(config.items(), (c) -> c);

        EditConfirmPanel confirmPanel = new EditConfirmPanel(save -> {
            if (save) {
                config.items().clear();
                config.items().addAll(this.items.stream()
                        .flatMap(w -> w.items().stream())
                        .collect(Collectors.toList()));
                mgr.savePanelConfig(config);
            }
        });
        setBackPanel(confirmPanel);
        setOpenParentWhenClosing(true);
    }

    public static EditPanel create(GUIPanelManager mgr, Player player, @NotNull PanelConfig config) {
        return new EditPanel(mgr, player, config);
    }


    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[size.getSize()];

        for (WeightItems<ItemConfig> weightItems : items) {
            if (weightItems.items().isEmpty())
                continue;

            ItemConfig itemConfig = weightItems.items().get(0);
            GUIPanelItem item = new GUIPanelItem(this, itemConfig);
            item.setItemBuilder((p) -> buildIconItem(p, itemConfig, weightItems));
            item.setClickListener((e, p) -> handleClick(p, e, weightItems));
            slots[weightItems.getSlot()] = item;
        }

        return slots;
    }

    private ItemStack buildIconItem(Player player, ItemConfig itemConfig, WeightItems<ItemConfig> weights) {
        return buildEditingItem(itemConfig, weights, player);
    }

    private void handleClick(Player player, InventoryClickEvent event, WeightItems<ItemConfig> weights) {
        if (InventoryAction.SWAP_WITH_CURSOR.equals(event.getAction())) {
            if (swapping != null) {
                int toIdx = weights.getSlot();
                int fromIdx = swapping.getSlot();

                weights.items().forEach(ic -> ic.setSlot(fromIdx));
                weights.setSlot(fromIdx);
                swapping.items().forEach(ic -> ic.setSlot(toIdx));
                swapping.setSlot(toIdx);

                items.add(swapping);
                swapping = null;

                if (event.getCursor() != null)
                    event.getCursor().setAmount(0);
                this.update();
                setChanged();
            }
        } else if (InventoryAction.CLONE_STACK.equals(event.getAction())) {
            int slot = getEmptySlotIndex();
            if (slot >= size.getSize())
                return;

            WeightItems<ItemConfig> weightsCopy = new WeightItems<>(slot, weights.items().stream()
                    .map(ic -> {
                        ic = mgr.cloneItemConfig(ic, ic.getWeight());
                        ic.setSlot(slot);
                        return ic;
                    })
                    .collect(Collectors.toList()));

            items.add(weightsCopy);
            this.update();
            setChanged();

        } else if (ClickType.LEFT.equals(event.getClick())) {
            new ItemEditPanel(mgr, player, weights, this).open(this);

        } else if (ClickType.RIGHT.equals(event.getClick())) {
            if (event.getCursor() == null)
                return;

            event.setCancelled(false);
            event.setResult(Event.Result.ALLOW);
            swapping = weights;
            items.remove(weights);
            setChanged();
            Bukkit.getScheduler().runTaskLater(OWNER, this::update, 1);
        }
    }


    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (InventoryAction.CLONE_STACK.equals(event.getAction()) || InventoryAction.SWAP_WITH_CURSOR.equals(event.getAction())) {
            Optional.ofNullable(selectPanelItem(event.getSlot()))
                    .ifPresent(i -> i.getClickListener().click(event, getPlayer()));
            return true;

        } else if (event.getAction().name().startsWith("PLACE_")) {
            if (swapping != null) {  // to empty slot (= suffix)
                int toIdx = event.getSlot();
                swapping.setSlot(toIdx);
                swapping.items().forEach(ic -> ic.setSlot(toIdx));

                items.add(swapping);
                swapping = null;
                event.setCursor(null);
                this.update();
                setChanged();

            } else {  // new entry
                ItemStack cursor = event.getCursor();
                if (cursor != null) {
                    int slot = event.getSlot();
                    if (slot < size.getSize()) {
                        ItemStack icon = cursor.clone();
                        icon.setAmount(1);
                        WeightItems<ItemConfig> newEntry = new WeightItems<>(slot, Lists.newArrayList(
                                new ItemConfig(slot, mgr.getDefaultActionCreator().create(), null, null, new GUIIcon(icon), 1)
                        ));
                        items.add(newEntry);
                        Bukkit.getScheduler().runTaskLater(OWNER, () -> {
                            event.getView().setCursor(null);
                            new ItemEditPanel(mgr, player, newEntry, this).open(this);
                        }, 1);
                        setChanged();

                    } else {
                        Bukkit.getScheduler().runTaskLater(OWNER, () -> event.getView().setCursor(null), 1);
                    }
                    player.getInventory().addItem(cursor);
                }
            }
            return true;
        }
        return super.onClick(event);
    }

    @Override
    public void onEvent(InventoryClickEvent event) {
        if (!getInventory().equals(event.getClickedInventory())) {
            if (swapping != null) {
                items.add(swapping);
                swapping = null;
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                event.setCursor(null);
                Bukkit.getScheduler().runTaskLater(OWNER, this::update, 1);
                setChanged();
            }
        }
    }

    @Override
    public void onEvent(InventoryDragEvent event) {
        if (swapping != null) {
            items.add(swapping);
            swapping = null;
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            Bukkit.getScheduler().runTaskLater(OWNER, () -> {
                event.getView().setCursor(null);
                this.update();
            }, 1);

            setChanged();
        }
    }

    public ItemStack buildEditingItem(ItemConfig itemConfig, WeightItems<ItemConfig> weightItems, Player player) {
        GUIIcon icon = itemConfig.getIcon();
        ItemStack itemStack = icon.buildItemStack(player);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
//            String displayName = meta.getDisplayName();
//            displayName += ChatColor.AQUA + " (クリックして編集)";
//            meta.setDisplayName(displayName);

            List<String> lore = Lists.newArrayList();
            lore.add(ChatColor.DARK_GRAY + "================================");

            ClickAction clickAction = itemConfig.getClickAction();
            List<String> description = formatAction(clickAction, player);
            lore.add(ChatColor.GOLD + "アクション: " + ChatColor.YELLOW + description.remove(0));
            lore.addAll(description.stream().map(s -> "  " + s).collect(Collectors.toList()));

            Condition clickCondition = itemConfig.getClickCondition();
            description = formatCondition(clickCondition, player);
            lore.add(ChatColor.GOLD + "実行条件: " + ChatColor.YELLOW + description.remove(0));
            lore.addAll(description.stream().map(s -> "  " + s).collect(Collectors.toList()));

            Condition viewCondition = itemConfig.getViewCondition();
            description = formatCondition(viewCondition, player);
            lore.add(ChatColor.GOLD + "表示条件: " + ChatColor.YELLOW + description.remove(0));
            lore.addAll(description.stream().map(s -> "  " + s).collect(Collectors.toList()));

            if (weightItems.items().size() > 1) {
                lore.add(ChatColor.GOLD + "切り替え:  " + ChatColor.YELLOW + weightItems.items().size() + " パターン");
            }

            lore.add(ChatColor.DARK_GRAY + "================================");

            if (meta.getLore() != null && !meta.getLore().isEmpty()) {
                lore.addAll(meta.getLore());
                lore.add(ChatColor.DARK_GRAY + "================================");
            }

            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public List<String> formatAction(@Nullable ClickAction action, Player player) {
        if (action != null) {
            List<String> description = action.getDescription(player);
            if (description != null) {
                return description;
            }

            ClickActionCreator<? extends @Nullable ClickAction> creator = mgr.getActionCreator(action.getClass());
            if (creator != null)
                return Lists.newArrayList(Optional.ofNullable(creator.getSelectIcon().getItemMeta())
                        .filter(ItemMeta::hasDisplayName)
                        .map(ItemMeta::getDisplayName)
                        .orElse(ChatColor.GRAY + "なし"));
        }
        return Lists.newArrayList(ChatColor.GRAY + "なし");
    }

    public List<String> formatCondition(@Nullable Condition condition, Player player) {
        if (condition != null) {
            List<String> description = condition.getDescription(player);
            if (description != null) {
                return description;
            }

            ConditionCreator<? extends @Nullable Condition> creator = mgr.getConditionCreator(condition.getClass());
            if (creator != null)
                return Lists.newArrayList(Optional.ofNullable(creator.getSelectIcon().getItemMeta())
                        .filter(ItemMeta::hasDisplayName)
                        .map(ItemMeta::getDisplayName)
                        .orElse(ChatColor.GRAY + "なし"));
        }
        return Lists.newArrayList(ChatColor.GRAY + "なし");
    }


    public int getEmptySlotIndex() {
        List<WeightItems<ItemConfig>> items = this.items.stream().sorted(Comparator.comparingInt(WeightItems::getSlot)).collect(Collectors.toList());

        int slot = 0;
        for (WeightItems<ItemConfig> weights : items) {
            if (slot < weights.getSlot())
                return slot;
            slot++;
        }
        return slot;
    }

    public List<WeightItems<ItemConfig>> items() {
        return items;
    }
    
    public void setChanged() {
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }



    public class EditConfirmPanel extends GUIPanel {

        private final Consumer<Boolean> done;

        public EditConfirmPanel(Consumer<Boolean> done) {
            super(EditPanel.this.player, ChatColor.DARK_RED + "変更を保存しますか？", GUISize.CHEST9X3, PanelItem.createBlankItem().getItemStack());
            this.done = done;
        }

        @Override
        public void open() {
            if (isChanged())
                super.open();
        }

        @Override
        public PanelItem[] build() {
            PanelItem[] slots = new PanelItem[getSize()];
            slots[12] = PanelItem.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "保存しない").setClickListener((e, p) -> {
                done.accept(false);
                destroy();
            });
            slots[14] = PanelItem.createItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "保存する").setClickListener((e, p) -> {
                done.accept(true);
                destroy();
            });
            return slots;
        }

    }


}
