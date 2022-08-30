package com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelPlugin;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_15_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class v1_15_R1 implements NMS {
    @Override
    public @Nullable String formatTellrawJson(CommandSender sender, String json) throws CommandSyntaxException {
        IChatBaseComponent chatComponent = ArgumentChatComponent.a().parse(new StringReader(json));
        Vec3D loc;
        Vec2F look;
        WorldServer world;

        if (sender instanceof org.bukkit.entity.Entity) {
            Location loc2 = ((org.bukkit.entity.Entity) sender).getLocation();
            loc = new Vec3D(loc2.getX(), loc2.getY(), loc2.getZ());
            look = new Vec2F(loc2.getYaw(), loc2.getPitch());
            world = ((CraftWorld) ((org.bukkit.entity.Entity) sender).getWorld()).getHandle();

        } else if (sender instanceof BlockCommandSender) {
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
                getListener(sender).base, loc, look, world, 0,  // permission level?
                sender.getName(),
                chatComponent,
                ((CraftServer) Bukkit.getServer()).getServer(),
                (sender instanceof org.bukkit.entity.Entity) ? ((CraftEntity) sender).getHandle() : null
        );

        net.minecraft.server.v1_15_R1.Entity entity = null;
        if (sender instanceof Player) {
            entity = ((CraftPlayer) sender).getHandle();
        } else if (sender instanceof org.bukkit.entity.Entity) {
            entity = ((CraftEntity) sender).getHandle();
        }

        IChatBaseComponent components = ChatComponentUtils.filterForDisplay(wrapper, chatComponent, entity, 0);

        return components.getLegacyString();
    }

    private static CommandListenerWrapper getListener(CommandSender sender) {
        if (sender instanceof Player) {
            return ((CraftPlayer) sender).getHandle().getCommandListener();
        }
        if (sender instanceof BlockCommandSender) {
            return ((CraftBlockCommandSender) sender).getWrapper();
        }
        if (sender instanceof CommandMinecart) {
            return ((CraftMinecartCommand) sender).getHandle().getCommandBlock().getWrapper();
        }
        if (sender instanceof RemoteConsoleCommandSender) {
            return ((DedicatedServer) MinecraftServer.getServer()).remoteControlCommandListener.getWrapper();
        }
        if (sender instanceof ConsoleCommandSender) {
            return ((CraftServer) sender.getServer()).getServer().getServerCommandListener();
        }
        if (sender instanceof ProxiedCommandSender) {
            return ((ProxiedNativeCommandSender) sender).getHandle();
        }

        throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
    }


    @Override
    public boolean executeFunction(CommandSender sender, NamespacedKey functionName) {
        SimpleFunctionWrapper function;
        System.out.println(functionName);
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

}
