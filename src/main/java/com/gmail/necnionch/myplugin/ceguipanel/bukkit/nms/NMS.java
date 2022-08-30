package com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @Nullable String formatTellrawJson(CommandSender sender, String json) throws CommandSyntaxException;

    boolean executeFunction(CommandSender sender, NamespacedKey functionName);

}
