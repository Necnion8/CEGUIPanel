package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickActionCreator;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.ConditionCreator;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.EmptyCondition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class ConditionSelectPanel extends GUIPanel {

    private final GUIPanelManager mgr;
    private final Consumer<Optional<ConditionCreator<?>>> accept;

    public ConditionSelectPanel(GUIPanelManager mgr, Player player, Consumer<Optional<ConditionCreator<?>>> accept) {
        super(player, ChatColor.DARK_PURPLE + "条件選択", GUISize.CHEST9X6, null);
        this.mgr = mgr;
        this.accept = accept;
    }

    @Override
    public int getSize() {
        Collection<ClickActionCreator<?>> creators = mgr.getActionCreators().values();
        int size = creators.size() + 1;
        if (9 > size)
            return 9;
        else if (18 > size)
            return 18;
        else if (27 > size)
            return 27;
        else if (36 > size)
            return 36;
        else if (45 > size)
            return 45;
        else
            return 54;
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[getSize()];

        Collection<ConditionCreator<?>> creators = mgr.getConditionCreators().values();

        slots[0] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                .setClickListener((e, p) -> accept.accept(Optional.empty()));

        slots[1] = PanelItem.createItem(Material.BARRIER, ChatColor.GOLD + "なし")
                .setClickListener((e, p) -> accept.accept(Optional.of(new EmptyCondition.Creator())));

        int slot = 2;
        for (ConditionCreator<?> creator : creators) {
            slots[slot++] = new PanelItem(creator.getSelectIcon()).setClickListener((e, p) -> accept.accept(Optional.of(creator)));
            if (slots.length <= slot)
                break;
        }

        return slots;
    }

}
