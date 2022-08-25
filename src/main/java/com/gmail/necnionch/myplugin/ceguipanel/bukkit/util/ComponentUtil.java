package com.gmail.necnionch.myplugin.ceguipanel.bukkit.util;

import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Set;

public class ComponentUtil {

    public static String componentToLegacy(BaseComponent... components) {
        StringBuilder builder = new StringBuilder();
        Set<Style> last = Sets.newHashSet();

        for (BaseComponent component : components) {
            Set<Style> styles = getStyle(component);
            if (last.stream().anyMatch(s -> !styles.contains(s))) {
                builder.append(ChatColor.RESET);
            }
            last = styles;
            builder.append(component.toLegacyText());
        }
        return builder.toString();
    }


    public static Set<Style> getStyle(BaseComponent component) {
        Set<Style> style = Sets.newHashSet();
        if (component.isBold()) {
            style.add(Style.BOLD);
        }
        if (component.isItalic()) {
            style.add(Style.ITALIC);
        }
        if (component.isObfuscated()) {
            style.add(Style.OBFUSCATED);
        }
        if (component.isStrikethrough()) {
            style.add(Style.STRIKETHROUGH);
        }
        if (component.isUnderlined()) {
            style.add(Style.UNDERLINED);
        }
        return style;
    }


    public enum Style {
        BOLD, ITALIC, OBFUSCATED, STRIKETHROUGH, UNDERLINED
    }
}
