package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class IconEditPanel extends GUIPanel {
    private final Consumer<Optional<ItemStack>> done;
    private final ItemStack model;
    private ItemStack change;
    private PanelItem overridable;
    private boolean cloneMeta;

    public IconEditPanel(Player player, ItemStack model, @Nullable ItemStack change, Consumer<Optional<ItemStack>> done) {
        super(player, ChatColor.DARK_GREEN + "アイコン設定", GUISize.CHEST9X3, null);
        this.done = done;
        this.model = model;
        this.change = change;
//        this.itemStack = itemStack.clone();
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
        return itemStack;
    }

}
