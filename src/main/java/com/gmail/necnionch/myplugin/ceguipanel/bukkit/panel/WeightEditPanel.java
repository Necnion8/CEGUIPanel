package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WeightEditPanel extends GUIPanel {
    private final GUIPanelManager mgr;
    private final Consumer<ItemConfig> done;
    private final WeightItems<ItemConfig> weights;
    private final EditPanel editPanel;
    private ItemConfig swapping;

    public WeightEditPanel(GUIPanelManager mgr, EditPanel editPanel, Player player, WeightItems<ItemConfig> weights, Consumer<ItemConfig> done) {
        super(player, ChatColor.DARK_GREEN + "表示アイテム設定 (優先順)", GUISize.CHEST9X6, null);
        this.mgr = mgr;
        this.editPanel = editPanel;
        this.done = done;
        this.weights = weights;
    }

    @Override
    public int getSize() {
        int size = weights.items().size() + 2;
        if (9 >= size)
            return 9;
        else if (18 >= size)
            return 18;
        else if (27 >= size)
            return 27;
        else if (36 >= size)
            return 36;
        else if (45 >= size)
            return 45;
        else
            return 54;
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[getSize()];

        slots[0] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                .setClickListener((e, p) -> {
                    if (!InventoryAction.CLONE_STACK.equals(e.getAction()) && !InventoryAction.SWAP_WITH_CURSOR.equals(e.getAction()))
                        finish(null);
                });

        int slot = 1;
        for (ItemConfig itemConfig : weights.items()) {
            slots[slot++] = new PanelItem(null)
                    .setItemBuilder((p) -> {
                        ItemStack itemStack = editPanel.buildEditingItem(itemConfig, weights, player);
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta != null) {
                            List<String> lore = Lists.newArrayList(
                                    ChatColor.GRAY + "左クリック: アイテム設定",
                                    ChatColor.GRAY + "右クリック: 並び替え",
                                    ChatColor.GRAY + "S+右クリック: 削除",
                                    ChatColor.GRAY + "ホイールクリック: 複製"
                            );
                            if (meta.getLore() != null) {
                                lore.addAll(meta.getLore());
                            }
                            meta.setLore(lore);
                            itemStack.setItemMeta(meta);
                        }
                        return itemStack;
                    })
                    .setClickListener((e, p) -> {
                        if (InventoryAction.SWAP_WITH_CURSOR.equals(e.getAction())) {  // switch complete (replace)
                            if (swapping != null) {
                                int idx = weights.items().indexOf(itemConfig);
                                weights.items().add(idx, swapping);
                                swapping = null;

                                if (e.getCursor() != null)
                                    e.getCursor().setAmount(0);
                                this.update();
                            }

                        } else if (InventoryAction.CLONE_STACK.equals(e.getAction())) {  // clone
                            weights.items().add(mgr.cloneItemConfig(itemConfig, weights.items().size() + 1));
                            this.update();

                        } else if (ClickType.LEFT.equals(e.getClick())) {  // open setting
                            finish(itemConfig);

                        } else if (ClickType.RIGHT.equals(e.getClick())) {  // switch
                            if (e.getCursor() == null)
                                return;
                            e.setCancelled(false);
                            e.setResult(Event.Result.ALLOW);
                            swapping = itemConfig;
                            weights.items().remove(itemConfig);
//                            this.update();
                            Bukkit.getScheduler().runTaskLater(OWNER, this::update, 1);

                        } else if (ClickType.SHIFT_RIGHT.equals(e.getClick())) {  // remove
                            if (weights.items().size() >= 2) {
                                weights.items().remove(itemConfig);
                                this.update();
                            }
                        }

                    });
            if (slots.length <= slot)
                break;
        }




        return slots;
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (InventoryAction.CLONE_STACK.equals(event.getAction()) || InventoryAction.SWAP_WITH_CURSOR.equals(event.getAction())) {
            Optional.ofNullable(selectPanelItem(event.getSlot()))
                    .ifPresent(i -> i.getClickListener().click(event, getPlayer()));
            return true;

        } else if (event.getAction().name().startsWith("PLACE_")) {
            if (swapping != null) {  // to empty slot (= suffix)
                weights.items().add(swapping);
                swapping = null;
                event.setCursor(null);
                this.update();
            }
            return true;
        }
        return super.onClick(event);
    }

    @Override
    public void onEvent(InventoryClickEvent event) {
        if (!getInventory().equals(event.getClickedInventory())) {
            if (swapping != null) {
                weights.items().add(swapping);
                swapping = null;
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                event.setCursor(null);
                Bukkit.getScheduler().runTaskLater(OWNER, this::update, 1);
            }
        }
    }

    @Override
    public void onEvent(InventoryDragEvent event) {
        if (swapping != null) {
            weights.items().add(swapping);
            swapping = null;
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            Bukkit.getScheduler().runTaskLater(OWNER, () -> {
                event.getView().setCursor(null);
                this.update();
            }, 1);
        }
    }

//    @Override
//    public void onEvent(InventoryCloseEvent event) {
//        finish(null);
//    }

    private void finish(ItemConfig selected) {
        int weight = 0;
        for (ItemConfig itemConfig : weights.items()) {
            itemConfig.setWeight(++weight);
        }
        done.accept(selected);
    }

}
