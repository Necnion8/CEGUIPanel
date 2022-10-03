package com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelPlugin;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentChatComponent;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class v1_19_R1 implements NMS {
    @Override
    public @Nullable String formatTellrawJson(CommandSender sender, String json) throws CommandSyntaxException {
        if (sender instanceof Entity)
            return convertJsonToString(Bukkit.getConsoleSender(), (Entity) sender, json);
        return convertJsonToString(sender, json);
    }

    private static CommandListenerWrapper getListener(CommandSender sender) {
        return ((CommandListenerWrapper) CommandAPIHandler.getInstance().getNMS().getCLWFromCommandSender(sender));
    }


    @Override
    public boolean executeFunction(CommandSender sender, NamespacedKey functionName) {
        SimpleFunctionWrapper function;
        try {
            function = CommandAPIHandler.getInstance().getNMS().getFunction(functionName);
        } catch (NoSuchElementException e) {
            function = null;
        }
        if (function == null) {
            JavaPlugin.getPlugin(GUIPanelPlugin.class).getLogger().warning("Failed to execute function: " + functionName);
            return false;
        }
        function.run(sender);
        return true;
    }


    // by SideboardPerPlayer

    private static final Map<Integer, ChatColor> colorCodeMap = Stream.of(ChatColor.values())
            .filter(cc -> Objects.nonNull(cc.getColor()))
            .collect(Collectors.toMap(cc -> (cc.getColor().getRed() & 0xFF) << 16 | ((cc.getColor().getGreen() & 0xFF) << 8) | ((cc.getColor().getBlue() & 0xFF)), cc -> cc));

    public static String convertJsonToString(@NotNull CommandSender sender, @NotNull Entity target, @NotNull String json) throws CommandSyntaxException {
        IChatBaseComponent chatComponent = ArgumentChatComponent.a().parse(new StringReader(json));
        Location loc = target.getLocation();

        CommandListenerWrapper wrapper = new CommandListenerWrapper(
                getListener(sender).c,
                new Vec3D(loc.getX(), loc.getY(), loc.getZ()),
                new Vec2F(loc.getYaw(), loc.getPitch()),
                ((CraftWorld) target.getWorld()).getHandle(),
                0,  // permission level?
                target.getName(),
                chatComponent,
                ((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftEntity) target).getHandle()
        );

        net.minecraft.world.entity.Entity entity = null;
        if (sender instanceof Player) {
            entity = ((CraftPlayer) sender).getHandle();
        } else if (sender instanceof Entity) {
            entity = ((CraftEntity) sender).getHandle();
        }

        IChatMutableComponent components = ChatComponentUtils.a(wrapper, chatComponent, entity, 0);

        return BaseComponent.toLegacyText(getBaseComponentsFromChatComponent(components));
    }

    public static String convertJsonToString(@NotNull CommandSender sender, @NotNull String json) throws CommandSyntaxException {
        if (sender instanceof Entity)  // bypass selector permission
            return convertJsonToString(Bukkit.getConsoleSender(), ((Entity) sender), json);

        IChatBaseComponent chatComponent = ArgumentChatComponent.a().parse(new StringReader(json));
        Vec3D loc;
        Vec2F look;
        WorldServer world;

        if (sender instanceof BlockCommandSender) {
            Block block = ((BlockCommandSender) sender).getBlock();
            Location loc2 = block.getLocation();
            loc = new Vec3D(loc2.getX(), loc2.getY(), loc2.getZ());
            look = new Vec2F(loc2.getYaw(), loc2.getPitch());
            world = ((CraftWorld) block.getWorld()).getHandle();
        } else {
            loc = new Vec3D(0, 0, 0);
            look = new Vec2F(0, 0);
            world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
        }

        CommandListenerWrapper wrapper = new CommandListenerWrapper(
                getListener(sender).c, loc, look, world, 0,  // permission level?
                sender.getName(),
                chatComponent,
                ((CraftServer) Bukkit.getServer()).getServer(),
                null
        );

        IChatBaseComponent components = ChatComponentUtils.a(wrapper, chatComponent, null, 0);

        return BaseComponent.toLegacyText(getBaseComponentsFromChatComponent(components));
    }

    public static BaseComponent[] getBaseComponentsFromChatComponent(IChatBaseComponent base) {
        ComponentBuilder b = new ComponentBuilder();

        ChatModifier defaultModifier;
        if (base.b() instanceof LiteralContents) {
            b.append(((LiteralContents) base.b()).a());
            applyChatModifier(b, base.a());
            defaultModifier = base.a();
        } else {
            defaultModifier = ChatModifier.a;
        }

        for (IChatBaseComponent component : base.c()) {
            b.append(component.getString(), ComponentBuilder.FormatRetention.NONE);
            if (component.a().toString().equals("{}")) {  // unset
                applyChatModifier(b, defaultModifier);
            } else {
                applyChatModifier(b, component.a());
            }
        }
        return b.create();
    }

    public static void applyChatModifier(ComponentBuilder builder, ChatModifier modifier) {
        Optional.ofNullable(modifier.a()).ifPresent(color ->
                builder.color(Optional.ofNullable(colorCodeMap.get(color.a()))
                        .orElseGet(() -> ChatColor.of(new Color(color.a()))))
        );

        if (modifier.b())
            builder.bold(true);
        if (modifier.c())
            builder.italic(true);
        if (modifier.d())
            builder.underlined(true);
        if (modifier.e())
            builder.strikethrough(true);
        if (modifier.f())
            builder.obfuscated(true);
    }

}
