package com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelPlugin;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @Nullable String formatTellrawJson(CommandSender sender, String json) throws CommandSyntaxException;

    boolean executeFunction(CommandSender sender, NamespacedKey functionName);

    default boolean executeFunction(CommandSender sender, String functionName) {
        String[] sp = functionName.split(":", 2);
        if (sp[0].isEmpty() || sp[1].isEmpty())
            return false;
        NamespacedKey key;
        try {
            key = new NamespacedKey(sp[0], sp[1]);
        } catch (IllegalArgumentException ignored) {
            JavaPlugin.getPlugin(GUIPanelPlugin.class).getLogger().severe("Failed to execute function: " + functionName);
            return false;
        }
        return executeFunction(sender, key);
    }

}
