package com.gmail.necnionch.myplugin.ceguipanel.bukkit;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.MainConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.config.PanelConfig;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.GUISize;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.nms.NMSHandler;
import com.gmail.necnionch.myplugin.ceguipanel.bukkit.panel.EditPanel;
import com.google.common.collect.Lists;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class GUIPanelPlugin extends JavaPlugin {
    private final MainConfig mainConfig = new MainConfig(this);
    private final GUIPanelManager panelManager = new GUIPanelManager(this, mainConfig);

    @Override
    public void onEnable() {
        boolean nmsComplete = false;
        try {
            nmsComplete = new NMSHandler(getLogger()).init();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (!nmsComplete) {
            setEnabled(false);
            return;
        }

        registerCommands();
        mainConfig.load();
        getServer().getServicesManager().register(GUIPanelManager.class, panelManager, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        Panel.destroyAll();
        getServer().getServicesManager().unregisterAll(this);
    }

    private void registerCommands() {
        Argument panelArgument = new StringArgument("panel")
                .overrideSuggestions((s, a) -> panelManager.getPanelNames().toArray(new String[0]));
        Argument guiSizeArgument = new StringArgument("size")
                .overrideSuggestions((s, a) -> Stream.of(GUISize.values()).map(ss -> ss.name().toLowerCase(Locale.ROOT)).toArray(String[]::new));

        new CommandAPICommand("ceguipanel")
                .withPermission("ceguipanel.command.ceguipanel")
                .withSubcommand(new CommandAPICommand("createpanel")
                        .withArguments(new StringArgument("name"))
                        .withArguments(guiSizeArgument)
                        .executes(this::execCreatePanel)
                )
                .withSubcommand(new CommandAPICommand("createpanel")
                        .withArguments(new StringArgument("name"))
                        .withArguments(guiSizeArgument)
                        .withArguments(new GreedyStringArgument("title"))
                        .executes(this::execCreatePanel)
                )
                .withSubcommand(new CommandAPICommand("removepanel")
                        .withArguments(panelArgument)
                        .executes(this::execRemovePanel)
                )
                .withSubcommand(new CommandAPICommand("clonepanel")
                        .withArguments(panelArgument)
                        .withArguments(new StringArgument("newName"))
                        .executes(this::execClonePanel)
                )
                .withSubcommand(new CommandAPICommand("clonepanel")
                        .withArguments(panelArgument)
                        .withArguments(new StringArgument("newName"))
                        .withArguments(guiSizeArgument)
                        .executes(this::execClonePanel)
                )
                .withSubcommand(new CommandAPICommand("clonepanel")
                        .withArguments(panelArgument)
                        .withArguments(new StringArgument("newName"))
                        .withArguments(guiSizeArgument)
                        .withArguments(new GreedyStringArgument("title"))
                        .executes(this::execClonePanel)
                )
                .withSubcommand(new CommandAPICommand("setpaneltitle")
                        .withArguments(panelArgument)
                        .withArguments(new GreedyStringArgument("title"))
                        .executes(this::execSetPanelTitle)
                )

                .withSubcommand(new CommandAPICommand("open")
                        .withArguments(panelArgument)
                        .executesNative(this::execOpen)
                )
                .withSubcommand(new CommandAPICommand("open")
                        .withArguments(panelArgument)
                        .withArguments(new EntitySelectorArgument("targets", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
                        .executesNative(this::execOpen)
                )
                .withSubcommand(new CommandAPICommand("edit")
                        .withArguments(panelArgument)
                        .executes(this::execEdit)
                )
                .withSubcommand(new CommandAPICommand("close")  // functionアクションを実行した後閉じるのに使う
                        .executesNative(this::execClose)
                )
                .withSubcommand(new CommandAPICommand("setnpc")
                        .withArguments(panelArgument)
                        .executes(this::execSetNPC)
                )
                .register();
    }


    public GUIPanelManager getPanelManager() {
        return panelManager;
    }

    public static void executeFunction(Player executor, String functionKey) {
        Location location = executor.getLocation();
        location.setY(-256);
        CommandMinecart sender = (CommandMinecart) executor.getWorld().spawnEntity(location, EntityType.MINECART_COMMAND);
        try {
            Bukkit.dispatchCommand(sender, String.format("execute as \"%s\" at @s run function %s", executor.getUniqueId(), functionKey));
        } finally {
            sender.remove();
        }
    }


    private int execClose(NativeProxyCommandSender s, Object[] objects) {
        CommandSender executor = s.getCallee();
        if (executor instanceof HumanEntity) {
            ((HumanEntity) executor).closeInventory();
        }
        return 1;
    }


    private int execCreatePanel(CommandSender sender, Object[] objects) {
        if (!(sender instanceof Player))
            return 0;
        String panelName = (String) objects[0];
        GUISize guiSize;
        try {
            guiSize = GUISize.valueOf(((String) objects[1]).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "パネルサイズが無効です");
            return 0;
        }
        String title;
        try {
            title = ChatColor.translateAlternateColorCodes('&', (String) objects[2]);
        } catch (IndexOutOfBoundsException e) {
            title = ChatColor.RESET.toString();
        }

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig != null) {
            sender.sendMessage(ChatColor.RED + "パネル " + panelName + " が既に存在します");
            return 0;
        }

        panelConfig = new PanelConfig(panelName, title, guiSize, Lists.newArrayList());
        EditPanel editor = EditPanel.create(panelManager, ((Player) sender).getPlayer(), panelConfig);
        editor.setChanged();
        editor.open();
        return 0;

    }

    private int execRemovePanel(CommandSender sender, Object[] objects) {
        String panelName = (String) objects[0];

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig == null) {
            sender.sendMessage(ChatColor.RED + "パネル " + panelName + " がありません");
            return 0;
        }

        panelManager.removePanelConfigByName(panelConfig.getName());
        sender.sendMessage(ChatColor.GOLD + "パネル " + panelName + " を削除しました");
        return 0;
    }

    private int execOpen(NativeProxyCommandSender s, Object[] objects) {
        CommandSender executor = s.getCallee();

        String panelName = (String) objects[0];
        List<Player> players;
        try {
            //noinspection unchecked
            players = (List<Player>) objects[1];
        } catch (IndexOutOfBoundsException e) {
            if (!(executor instanceof Player)) {
                executor.sendMessage(ChatColor.RED + "プレイヤーを指定してください");
                return 0;
            }
            players = Lists.newArrayList((Player) executor);
        }

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig == null) {
            executor.sendMessage(ChatColor.RED + "パネル " + panelName + " がありません");
            return 0;
        }

        players.forEach(p -> panelManager.createGUIPanel(panelConfig, p).open());
        return 0;
    }

    private int execEdit(CommandSender sender, Object[] objects) {
        if (!(sender instanceof Player))
            return 0;
        String panelName = (String) objects[0];

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig == null) {
            sender.sendMessage(ChatColor.RED + "パネル " + panelName + " がありません");
            return 0;
        }

        EditPanel.create(panelManager, ((Player) sender), panelConfig).open();
        return 0;
    }

    private int execClonePanel(CommandSender sender, Object[] objects) {
        if (!(sender instanceof Player))
            return 0;
        String panelName = (String) objects[0];
        String newPanelName = (String) objects[1];
        GUISize guiSize = null;
        String title = null;
        try {
            guiSize = GUISize.valueOf(((String) objects[2]).toUpperCase(Locale.ROOT));
            title = (String) objects[3];
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "パネルサイズが無効です");
            return 0;
        } catch (IndexOutOfBoundsException ignored) {
        }

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig == null) {
            sender.sendMessage(ChatColor.RED + "パネル " + panelName + " がありません");
            return 0;
        }

        panelConfig = panelManager.clonePanelConfig(panelConfig, newPanelName);

        if (guiSize != null)
            panelConfig.setSize(guiSize);
        if (title != null)
            panelConfig.setTitle(title);

        EditPanel editor = EditPanel.create(panelManager, ((Player) sender).getPlayer(), panelConfig);
        editor.setChanged();
        editor.open();
        return 0;
    }

    private int execSetPanelTitle(CommandSender sender, Object[] objects) {
        String panelName = (String) objects[0];
        String title = (String) objects[1];

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig == null) {
            sender.sendMessage(ChatColor.RED + "パネル " + panelName + " がありません");
            return 0;
        }

        panelConfig.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        panelManager.savePanelConfig(panelConfig);
        sender.sendMessage(ChatColor.GOLD + "タイトルを変更しました");
        return 0;
    }

    private int execSetNPC(CommandSender sender, Object[] objects) {
        String panelName = (String) objects[0];

        PanelConfig panelConfig = panelManager.getPanelConfigByName(panelName);
        if (panelConfig == null) {
            sender.sendMessage(ChatColor.RED + "パネル " + panelName + " がありません");
            return 0;
        }

        ((Player) sender).performCommand("npc command add ceguipanel open " + panelName + " \"\"%player%\"\"");
        return 0;
    }

}
