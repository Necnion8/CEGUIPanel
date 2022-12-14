package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickActionCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class ActionSelectPanel extends GUIPanel {

    private final GUIPanelManager mgr;
    private final Consumer<Optional<ClickActionCreator<?>>> accept;

    public ActionSelectPanel(GUIPanelManager mgr, Player player, Consumer<Optional<ClickActionCreator<?>>> accept) {
        super(player, ChatColor.DARK_PURPLE + "アクション選択", GUISize.CHEST9X6, null);
        this.mgr = mgr;
        this.accept = accept;
    }

    @Override
    public int getSize() {
        Collection<ClickActionCreator<?>> creators = mgr.getActionCreators().values();
        if (9 > creators.size())
            return 9;
        else if (18 > creators.size())
            return 18;
        else if (27 > creators.size())
            return 27;
        else if (36 > creators.size())
            return 36;
        else if (45 > creators.size())
            return 45;
        else
            return 54;
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[getSize()];

        Collection<ClickActionCreator<?>> creators = mgr.getActionCreators().values();

        slots[0] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                .setClickListener((e, p) -> accept.accept(Optional.empty()));

        int slot = 1;
        for (ClickActionCreator<?> creator : creators) {
            slots[slot++] = new PanelItem(creator.getSelectIcon()).setClickListener((e, p) -> accept.accept(Optional.of(creator)));
            if (slots.length <= slot)
                break;
        }

        return slots;
    }


}
