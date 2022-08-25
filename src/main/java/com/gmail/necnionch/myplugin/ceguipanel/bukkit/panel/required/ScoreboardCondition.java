package com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.required;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUIPanel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class ScoreboardCondition implements ClickCondition {

    private @Nullable final String objective;
    private @Nullable final String name;
    private final @NotNull MatchType matchType;
    private final int value;

    public ScoreboardCondition(@Nullable String objective, @Nullable String name, @NotNull MatchType matchType, int value) {
        this.objective = objective;
        this.name = name;
        this.matchType = matchType;
        this.value = value;
    }

    public @Nullable String getObjective() {
        return objective;
    }

    public @Nullable String getName() {
        return name;
    }

    public @NotNull MatchType getMatchType() {
        return matchType;
    }

    public int getValue() {
        return value;
    }


    @Override
    public boolean check(GUIPanel panel, Player player) {
        return false;
    }


    public static class Creator implements ClickConditionCreator<ScoreboardCondition> {

        @Override
        public @NotNull ScoreboardCondition create() {
            return new ScoreboardCondition(null, null, MatchType.EQUAL, 1);
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
    }

    public interface ScorePredicate {
        boolean check(int value, int value2);
    }

}
