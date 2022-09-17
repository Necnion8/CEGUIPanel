package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelManager;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIIcon;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.action.ClickAction;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.Condition;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition.EmptyCondition;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.util.LineInput;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ItemEditPanel extends GUIPanel {

    private final GUIPanelManager mgr;
    private final EditPanel editPanel;
    private final LoreEditor loreEditor;
    private ItemConfig currentItemConfig;
    private final WeightItems<ItemConfig> weights;
    private PanelItem swappable;

    public ItemEditPanel(GUIPanelManager mgr, Player player, WeightItems<ItemConfig> weights, EditPanel editPanel) {
        super(player, ChatColor.DARK_RED + "アイテム編集 " + ChatColor.DARK_GRAY + "(1/" + weights.items().size() + ")",
                GUISize.CHEST9X5, PanelItem.createBlankItem().getItemStack());
        this.mgr = mgr;
        this.weights = weights;
        this.editPanel = editPanel;
        this.currentItemConfig = weights.items().get(0);
        this.loreEditor = new LoreEditor();
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[size.getSize()];

        ItemConfig config = this.currentItemConfig;

        slots[10] = swappable = new PanelItem(editPanel.buildEditingItem(config, weights, player))
                .setClickListener((e, p) -> {
                    if (!InventoryAction.SWAP_WITH_CURSOR.equals(e.getAction()))
                        return;
                    ItemStack cursor = e.getCursor();
                    if (cursor == null)
                        return;
                    e.setCursor(null);
                    p.getInventory().addItem(cursor);

                    new IconEditPanel(player, config.getIcon().getItemStack(), cursor, r -> {
                        r.ifPresent(itemStack -> {
                            config.getIcon().setItemStack(itemStack);
                            editPanel.setChanged();
                        });
                        this.open();
                    }).open(this);
                });

        slots[13] = new PanelItem(null)
                .setItemBuilder((p) -> {
                    List<String> lore = Lists.newArrayList();
                    if (config.getClickAction() != null) {
                        lore.add(ChatColor.GRAY + "左クリック: 設定を編集");
                        lore.add(ChatColor.GRAY + "右クリック: 別のアクションに変更");
                    } else {
                        lore.add(ChatColor.GRAY + "クリック: アクションを選択");
                    }
                    return PanelItem.createItem(Material.FLINT,
                            ChatColor.RED + "アクション設定" + ChatColor.GRAY + " - " + ChatColor.WHITE +
                                    editPanel.formatAction(config.getClickAction(), player).get(0), lore).getItemStack();
                })
                .setClickListener((e, p) -> {
                    if (config.getClickAction() == null || ClickType.RIGHT.equals(e.getClick())) {
                        new ActionSelectPanel(mgr, player, result -> {
                            if (result.isPresent()) {
                                ClickAction clickAction = result.get().create();

                                clickAction.openSettingGUI(player, this, config, () -> {
                                    config.setClickAction(clickAction);
                                    editPanel.setChanged();
                                    this.open();
                                });

                            } else {
                                this.open();
                            }
                        }).open(this);

                    } else if (ClickType.LEFT.equals(e.getClick())) {
                        config.getClickAction().openSettingGUI(player, this, config, this::open);
                        editPanel.setChanged();
                    }
                });

        slots[14] = new PanelItem(null)
                .setItemBuilder((p) -> {
                    List<String> lore = Lists.newArrayList();
                    if (config.getClickCondition() != null) {
                        lore.add(ChatColor.GRAY + "左クリック: 設定を編集");
                        lore.add(ChatColor.GRAY + "右クリック: 別の条件に変更");
                    } else {
                        lore.add(ChatColor.GRAY + "クリック: 実行条件を選択");
                    }
                    return PanelItem.createItem(Material.FLINT,
                            ChatColor.RED + "実行条件の設定" + ChatColor.GRAY + " - " + ChatColor.WHITE +
                                    editPanel.formatCondition(config.getClickCondition(), player).get(0), lore).getItemStack();
                })
                .setClickListener((e, p) -> {
                    if (config.getClickCondition() == null || ClickType.RIGHT.equals(e.getClick())) {
                        new ConditionSelectPanel(mgr, player, result -> {
                            if (result.isPresent()) {
                                Condition condition = result.get().create();
                                if (condition instanceof EmptyCondition) {
                                    config.setClickCondition(null);
                                    this.open();
                                    editPanel.setChanged();
                                } else {
                                    condition.openSettingGUI(player, this, config, () -> {
                                        config.setClickCondition(condition);
                                        this.open();
                                        editPanel.setChanged();
                                    });
                                }
                            } else {
                                this.open();
                            }
                        }).open(this);

                    } else if (ClickType.LEFT.equals(e.getClick())) {
                        Optional.ofNullable(config.getClickCondition()).ifPresent(cond -> {
                            cond.openSettingGUI(player, this, config, this::open);
                            editPanel.setChanged();
                        });
                    }
                });

        slots[15] = new PanelItem(null)
                .setItemBuilder((p) -> {
                    List<String> lore = Lists.newArrayList();
                    if (config.getViewCondition() != null) {
                        lore.add(ChatColor.GRAY + "左クリック: 設定を編集");
                        lore.add(ChatColor.GRAY + "右クリック: 別の条件に変更");
                    } else {
                        lore.add(ChatColor.GRAY + "クリック: 表示条件を選択");
                    }
                    return PanelItem.createItem(Material.FLINT,
                            ChatColor.RED + "表示条件の設定" + ChatColor.GRAY + " - " + ChatColor.WHITE +
                                    editPanel.formatCondition(config.getViewCondition(), player).get(0), lore).getItemStack();
                })
                .setClickListener((e, p) -> {
                    if (config.getViewCondition() == null || ClickType.RIGHT.equals(e.getClick())) {
                        new ConditionSelectPanel(mgr, player, result -> {
                            if (result.isPresent()) {
                                Condition condition = result.get().create();
                                if (condition instanceof EmptyCondition) {
                                    config.setViewCondition(null);
                                    this.open();
                                    editPanel.setChanged();
                                } else {
                                    condition.openSettingGUI(player, this, config, () -> {
                                        config.setViewCondition(condition);
                                        this.open();
                                        editPanel.setChanged();
                                    });
                                }
                            } else {
                                this.open();
                            }
                        }).open(this);

                    } else if (ClickType.LEFT.equals(e.getClick())) {
                        Optional.ofNullable(config.getViewCondition()).ifPresent(cond -> {
                            cond.openSettingGUI(player, this, config, this::open);
                            editPanel.setChanged();
                        });
                    }
                });

        List<ItemConfig> weights = this.weights.items();
        slots[16] = new PanelItem(null)
                .setItemBuilder((p) -> {
                    List<String> lore = Lists.newArrayList();
                    if (weights.size() >= 2) {
                        lore.add(ChatColor.GRAY + "左クリック: 次の表示アイテムへ");
                        lore.add(ChatColor.GRAY + "右クリック: 表示アイテムの設定");
                    } else {
                        lore.add(ChatColor.GRAY + "クリック: 表示アイテムの設定");
                    }
                    return PanelItem.createItem(Material.TORCH, ChatColor.RED + "表示切り替えの設定" + ChatColor.WHITE + " [" + (weights.indexOf(config)+1) + "/" + weights.size() + "]", lore).getItemStack();
                })
                .setClickListener((e, p) -> {
                    if (weights.size() <= 1 || ClickType.RIGHT.equals(e.getClick())) {
                        new WeightEditPanel(mgr, editPanel, player, this.weights, selected -> {
                            if (selected != null && this.weights.items().contains(selected))
                                currentItemConfig = selected;
                            this.open();
                            editPanel.setChanged();
                        }).open(this);

                    } else if (ClickType.LEFT.equals(e.getClick())) {
                        int idx = weights.indexOf(config);
                        if (idx+1 < weights.size()) {
                            currentItemConfig = weights.get(idx+1);
                        } else {
                            currentItemConfig = weights.get(0);
                        }
                        this.update();
                    }
                });

        slots[28] = new PanelItem(null)
                .setItemBuilder((p) -> PanelItem.createItem(Material.NAME_TAG, ChatColor.RED + "表示名の変更", Lists.newArrayList(
                        ChatColor.GRAY + "左クリック: 表示名を入力",
                        ChatColor.GRAY + "右クリック: 表示名をリセット",
                        "",
                        ChatColor.GRAY + "> " + ChatColor.WHITE + ChatColor.ITALIC + Optional.ofNullable(config.getIcon().getItemStack().getItemMeta())
                                .filter(ItemMeta::hasDisplayName)
                                .map(ItemMeta::getDisplayName)
                                .orElseGet(() -> ChatColor.GRAY.toString() + ChatColor.ITALIC + "デフォルト")
                )).getItemStack())
                .setClickListener((e, p) -> {
                    if (ClickType.LEFT.equals(e.getClick())) {
                        GUIIcon icon = config.getIcon();
                        String name = Optional.ofNullable(icon.getItemStack().getItemMeta()).map(ItemMeta::getDisplayName).orElse("");

                        destroy(true);
                        p.spigot().sendMessage(new ComponentBuilder()
                                .append("= ").color(ChatColor.DARK_GRAY)
                                .append("表示名をチャット欄に入力してください ").color(ChatColor.GOLD)
                                .append("[中止]").color(ChatColor.DARK_AQUA)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"))
                                .create()
                        );
                        p.spigot().sendMessage(new ComponentBuilder()
                                .append("= ").color(ChatColor.DARK_GRAY)
                                .append("現在の値: ").color(ChatColor.WHITE)
                                .append(name.replace('§', '&')).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, name.replace('§', '&')))
                                .create());

                        LineInput.listen(OWNER, p, r -> {
                            r.ifPresent(s -> {
                                ItemMeta meta = icon.getItemStack().getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', s));
                                    icon.getItemStack().setItemMeta(meta);
                                    editPanel.setChanged();
                                }
                            });
                            this.open();
                        });

                    } else if (ClickType.RIGHT.equals(e.getClick())) {
                        Optional.ofNullable(config.getIcon().getItemStack().getItemMeta()).ifPresent(meta -> {
                            meta.setDisplayName(null);
                            config.getIcon().getItemStack().setItemMeta(meta);
                            editPanel.setChanged();
                            this.update();
                        });
                    }
                });

        slots[29] = new PanelItem(null)
                .setItemBuilder((p) -> PanelItem.createItem(Material.WRITABLE_BOOK, ChatColor.RED + "説明文の変更", loreEditor.buildEditIconLore()).getItemStack())
                .setClickListener((e, p) -> loreEditor.onClick(e));

        slots[34] = PanelItem.createItem(Material.BARRIER, ChatColor.RED + "削除").setClickListener((e, p) -> {
            if (!ClickType.LEFT.equals(e.getClick()))
                return;
            editPanel.items().remove(this.weights);
            editPanel.setChanged();
            editPanel.open();
        });

        slots[40] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.GOLD + "戻る").setClickListener((e, p) -> {
            if (!ClickType.LEFT.equals(e.getClick()))
                return;
            editPanel.open();
        });

        return slots;
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (InventoryAction.SWAP_WITH_CURSOR.equals(event.getAction())) {
            PanelItem item = selectPanelItem(event.getSlot());
            if (swappable.equals(item)) {
                item.getClickListener().click(event, getPlayer());
                return true;
            }
        }
        return super.onClick(event);
    }

    @Override
    public String getTitle() {
        int num = 1 + weights.items().indexOf(currentItemConfig);
        return ChatColor.DARK_RED + "アイテム編集 " + ChatColor.DARK_GRAY + "(" + num + "/" + weights.items().size() + ")";
    }



    private class LoreEditor {
        private int cursor = 0;

        public List<String> buildEditIconLore() {
            ItemMeta meta = currentItemConfig.getIcon().getItemStack().getItemMeta();
            if (meta == null)
                return Collections.emptyList();

            List<String> display = Lists.newArrayList();
            List<String> lore = Optional.ofNullable(meta.getLore()).orElseGet(Lists::newArrayList);
            int lines = lore.size();

            boolean newLine = cursor >= lines;

            display.add(ChatColor.GRAY + (newLine ? "左クリック: 新しい行を追加" : "左クリック: 現在の行を編集"));
            display.add(ChatColor.GRAY + "右クリック: 次の行を選択");
            display.add(ChatColor.GRAY + (newLine ? "S+右クリック: " : "S+右クリック: 現在の行を削除"));
            display.add("");

            for (int i = 0; i < lore.size(); i++) {
                display.add((cursor == i ? ChatColor.GOLD : ChatColor.GRAY) + "> " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + lore.get(i));
            }

            if (newLine)
                display.add(ChatColor.GOLD + "> " + ChatColor.GRAY + ChatColor.ITALIC + "新しく追加する？");

            return display;
        }

        public void nextCursor() {
            ItemMeta meta = currentItemConfig.getIcon().getItemStack().getItemMeta();
            if (meta == null) {
                cursor = 0;
                return;
            }
            int lines = Optional.ofNullable(meta.getLore()).map(List::size).orElse(0);
            if (lines <= 0) {
                cursor = 0;
            }

            cursor++;  // 1
            if (cursor > lines) {  // newLine +1
                cursor = 0;
            }
        }

        public @Nullable String getCurrentLine() {
            ItemMeta meta = currentItemConfig.getIcon().getItemStack().getItemMeta();
            if (meta == null)
                return null;
            List<String> lore = Optional.ofNullable(meta.getLore()).orElseGet(Lists::newArrayList);
            if (cursor >= lore.size())
                return null;
            return lore.get(cursor);
        }

        public void setCurrentLine(String line) {
            ItemMeta meta = currentItemConfig.getIcon().getItemStack().getItemMeta();
            if (meta == null)
                return;
            List<String> lore = Optional.ofNullable(meta.getLore()).orElseGet(Lists::newArrayList);
            if (cursor >= lore.size()) {
                lore.add(line);
            } else {
                lore.remove(cursor);
                lore.add(cursor, line);
            }
            meta.setLore(lore);
            currentItemConfig.getIcon().getItemStack().setItemMeta(meta);
            editPanel.setChanged();
        }

        public void removeCurrentLine() {
            ItemMeta meta = currentItemConfig.getIcon().getItemStack().getItemMeta();
            if (meta == null)
                return;
            List<String> lore = Optional.ofNullable(meta.getLore()).orElseGet(Lists::newArrayList);
            if (cursor < lore.size()) {
                lore.remove(cursor);
                meta.setLore(lore);
                currentItemConfig.getIcon().getItemStack().setItemMeta(meta);
                editPanel.setChanged();
            }
        }

        public void onClick(InventoryClickEvent event) {
            if (ClickType.LEFT.equals(event.getClick())) {
                // edit line
                String line = Optional.ofNullable(getCurrentLine()).orElse("");

                destroy(true);
                player.spigot().sendMessage(new ComponentBuilder()
                        .append("= ").color(ChatColor.DARK_GRAY)
                        .append("行テキストをチャット欄に入力してください ").color(ChatColor.GOLD)
                        .append("[中止]").color(ChatColor.DARK_AQUA)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"))
                        .create()
                );
                player.spigot().sendMessage(new ComponentBuilder()
                        .append("= ").color(ChatColor.DARK_GRAY)
                        .append("現在の値: ").color(ChatColor.WHITE)
                        .append(line.replace('§', '&'))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, line.replace('§', '&')))
                        .create());

                LineInput.listen(OWNER, player, r -> {
                    r.ifPresent(s -> {
                        setCurrentLine(ChatColor.translateAlternateColorCodes('&', s));
                    });
                    ItemEditPanel.this.open();
                });

            } else if (ClickType.RIGHT.equals(event.getClick())) {
                // next line
                nextCursor();
                ItemEditPanel.this.update();
            } else if (ClickType.SHIFT_RIGHT.equals(event.getClick())) {
                // remove line
                removeCurrentLine();
                ItemEditPanel.this.update();
            }
        }

    }

}
