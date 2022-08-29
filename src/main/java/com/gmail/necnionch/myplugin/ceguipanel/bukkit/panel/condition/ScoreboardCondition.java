package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.condition;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.ItemConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import com.google.common.collect.Lists;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScoreboardCondition implements Condition {

    private @Nullable String objective;
    private @Nullable String name;
    private @NotNull MatchType matchType;
    private int value;

    public ScoreboardCondition(@Nullable String objective, @Nullable String name, @NotNull MatchType matchType, int value) {
        this.objective = objective;
        this.name = name;
        this.matchType = matchType;
        this.value = value;
    }

    public ScoreboardCondition() {
        this(null, null, MatchType.EQUAL_OR_HIGH, 1);
    }

    public @Nullable String getObjective() {
        return objective;
    }

    public void setObjective(@Nullable String objective) {
        this.objective = objective;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @NotNull MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(@NotNull MatchType matchType) {
        this.matchType = matchType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean check(GUIPanel panel, Player player) {
        if (objective == null || name == null)
            return false;
        ScoreboardManager mgr = Bukkit.getScoreboardManager();
        if (mgr == null)
            return false;
        Scoreboard sb = mgr.getMainScoreboard();

        Objective obj = sb.getObjective(this.objective);
        if (obj == null)
            return false;

        String target = name;
        try {
            List<Entity> entities = Bukkit.selectEntities(player, name);
            if (!entities.isEmpty())
                target = entities.get(0).getName();
        } catch (IllegalArgumentException ignored) {
        }

        Score score = obj.getScore(target);
        return matchType.test(value, score.getScore());
    }

    @Override
    public @Nullable List<String> getDescription(Player player) {
        if (objective == null || name == null)
            return null;

        String localizedName = matchType.getLocalizedName();
        return Lists.newArrayList(
                "スコアボードの値",
                ChatColor.GRAY + "対象: " + ChatColor.WHITE + name + ChatColor.GRAY + " (" + objective + ")",
                ChatColor.GRAY + "比較: " + ChatColor.WHITE + "スコア " + value + " " + localizedName
        );
    }

    @Override
    public void serialize(Map<String, Object> config) {
        config.put("objective", objective);
        config.put("name", name);
        config.put("match_type", matchType.name().toLowerCase(Locale.ROOT));
        config.put("value", value);
    }

    @Override
    public void openSettingGUI(Player player, Panel parent, ItemConfig itemConfig, Runnable done) {
        new Configurator(player, done).open(parent);
    }


    public class Configurator extends Panel {
        private final Runnable done;

        public Configurator(Player player, Runnable done) {
            super(player, 27, ChatColor.DARK_AQUA + "スコア設定");
            this.done = done;
        }

        @Override
        public PanelItem[] build() {
            PanelItem[] slots = new PanelItem[getSize()];

            slots[10] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(
                            Material.OAK_SIGN, ChatColor.GOLD + "スコアボード名を入力", Lists.newArrayList(
                                    ChatColor.GRAY + "対象: " + Optional.ofNullable(objective).orElse("なし"))
                            ).getItemStack()
                    )
                    .setClickListener((e, p) -> {
                        new AnvilGUI.Builder()
                                .plugin(Panel.OWNER)
                                .title("スコアボード名を入力")
                                .text(Optional.ofNullable(objective).orElse(""))
                                .onComplete((p1, s) -> {
                                    objective = s;

                                    Bukkit.getScheduler().runTaskLater(Panel.OWNER, (@Nullable Runnable) this::open, 2);
                                    return AnvilGUI.Response.text("");
                                })
                                .open(p);
                    });

            slots[11] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(
                                    Material.OAK_SIGN, ChatColor.GOLD + "対象を入力", Lists.newArrayList(
                                            ChatColor.GRAY + "対象: " + Optional.ofNullable(name).orElse("なし"))
                            ).getItemStack()
                    )
                    .setClickListener((e, p) -> {
                        new AnvilGUI.Builder()
                                .plugin(Panel.OWNER)
                                .title("対象を入力")
                                .text(Optional.ofNullable(name).orElse(""))
                                .onComplete((p1, s) -> {
                                    name = s;

                                    Bukkit.getScheduler().runTaskLater(Panel.OWNER, (@Nullable Runnable) this::open, 2);
                                    return AnvilGUI.Response.text("");
                                })
                                .open(p);
                    });

            slots[13] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(
                                    Material.OAK_SIGN, ChatColor.GOLD + "スコア値を入力", Lists.newArrayList(
                                            ChatColor.GRAY + "スコア: " + value)
                            ).getItemStack()
                    )
                    .setClickListener((e, p) -> {
                        new AnvilGUI.Builder()
                                .plugin(Panel.OWNER)
                                .title("スコア値を入力")
                                .text("" + value)
                                .onComplete((p1, s) -> {
                                    int value;
                                    try {
                                        value = Integer.parseInt(s);
                                    } catch (NumberFormatException ex) {
                                        return AnvilGUI.Response.text("");
                                    }
                                    ScoreboardCondition.this.value = value;

                                    Bukkit.getScheduler().runTaskLater(Panel.OWNER, (@Nullable Runnable) this::open, 2);
                                    return AnvilGUI.Response.text("");
                                })
                                .open(p);
                    });

            slots[14] = new PanelItem(null)
                    .setItemBuilder((p) -> PanelItem.createItem(
                            Material.OAK_SIGN, ChatColor.GOLD + "比較タイプ " + ChatColor.GRAY + "- " + ChatColor.YELLOW + value + " " + matchType.getLocalizedName()
                            ).getItemStack())
                    .setClickListener((e, p) -> {
                        ArrayList<MatchType> values = Lists.newArrayList(MatchType.values());
                        int idx = values.indexOf(matchType);
                        if (idx+1 < values.size()) {
                            matchType = values.get(idx+1);
                        } else {
                            matchType = values.get(0);
                        }
                        this.update();
                    });

            slots[16] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "戻る")
                    .setClickListener((e, p) -> done.run());
            return slots;
        }

        @Override
        public boolean onClick(InventoryClickEvent event) {
            if (InventoryAction.SWAP_WITH_CURSOR.equals(event.getAction())) {
                PanelItem item = selectPanelItem(event.getSlot());
                item.getClickListener().click(event, getPlayer());
                return true;
            }
            return super.onClick(event);
        }

    }


    public static class Creator implements ConditionCreator<ScoreboardCondition> {

        @Override
        public @NotNull ScoreboardCondition create() {
            return new ScoreboardCondition();
        }

        @Override
        public @Nullable ScoreboardCondition createFromConfig(Map<?, ?> config) {
            String objective = (String) config.get("objective");
            String name = (String) config.get("name");
            MatchType matchType;;
            try {
                matchType = MatchType.valueOf((((String) config.get("match_type")).toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                 matchType = MatchType.EQUAL;
            }
            int value = 0;
            try {
                value = (Integer) config.get("value");
            } catch (ClassCastException ignored) {
            }
            return new ScoreboardCondition(objective, name, matchType, value);
        }

        @Override
        public @NotNull ItemStack getSelectIcon() {
            return PanelItem.createItem(Material.ACACIA_SIGN, ChatColor.GOLD + "スコア条件").getItemStack();
        }

        @Override
        public @NotNull String getConditionId() {
            return "scoreboard";
        }
    }


    public enum MatchType {
        EQUAL("==", (v1, v2) -> v1 == v2),
        EQUAL_OR_HIGH("<=", (v1, v2) -> v1 <= v2),
        EQUAL_OR_LOW(">=", (v1, v2) -> v1 >= v2),
        HIGH("<", (v1, v2) -> v1 < v2),
        LOW(">", (v1, v2) -> v1 > v2);

        private final ScorePredicate test;
        private final String name;

        MatchType(String name, ScorePredicate test) {
            this.name = name;
            this.test = test;
        }

        public boolean test(int value, int value2) {
            return test.check(value, value2);
        }

        public String getName() {
            return name;
        }

        public String getLocalizedName() {
            String matchTypeName = name();
            switch (this) {
                case LOW:
                    matchTypeName = "より小さい";
                    break;
                case HIGH:
                    matchTypeName = "より大きい";
                    break;
                case EQUAL:
                    matchTypeName = "に等しい";
                    break;
                case EQUAL_OR_LOW:
                    matchTypeName = "以下";
                    break;
                case EQUAL_OR_HIGH:
                    matchTypeName = "以上";
                    break;
            }
            return matchTypeName;
        }
    }

    public interface ScorePredicate {
        boolean check(int value, int value2);
    }

}
