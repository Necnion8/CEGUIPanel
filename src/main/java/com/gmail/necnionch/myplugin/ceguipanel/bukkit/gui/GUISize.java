package com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui;

import org.bukkit.event.inventory.InventoryType;

public enum GUISize {
    CHEST9X1(InventoryType.CHEST, 9),
    CHEST9X2(InventoryType.CHEST, 18),
    CHEST9X3(InventoryType.CHEST, 27),
    CHEST9X4(InventoryType.CHEST, 36),
    CHEST9X5(InventoryType.CHEST, 45),
    CHEST9X6(InventoryType.CHEST, 54),
    HOPPER5X1(InventoryType.HOPPER, 0),
    DROPPER3X3(InventoryType.DROPPER, 0);

    private final InventoryType type;
    private final int size;

    GUISize(InventoryType type, int size) {
        this.type = type;
        this.size = size;
    }

    public InventoryType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

}
