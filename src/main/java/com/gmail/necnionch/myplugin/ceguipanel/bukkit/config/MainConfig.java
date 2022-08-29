package com.gmail.necnionch.myplugin.ceguipanel.bukkit.config;

import com.gmail.necnionch.myplugin.ceguipanel.bukkit.GUIPanelPlugin;
import com.gmail.necnionch.myplugin.ceguipanel.common.BukkitConfigDriver;
import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MainConfig extends BukkitConfigDriver {
    private final Map<String, PanelConfig> panelEntries = Maps.newHashMap();
    private final GUIPanelPlugin plugin;

    public MainConfig(GUIPanelPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }


    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {
            panelEntries.clear();
            ConfigurationSection panelsSection = config.getConfigurationSection("panels");
            if (panelsSection != null) {
                for (String panelName : panelsSection.getKeys(false)) {
                    Map<String, Object> data = panelsSection.getConfigurationSection(panelName).getValues(true);
                    PanelConfig panelConfig;
                    try {
                        panelConfig = PanelConfig.deserialize(plugin.getPanelManager(), panelName, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    panelEntries.put(panelName, panelConfig);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        config = new YamlConfiguration();
        panelEntries.values().forEach(panelConfig -> config.set(
                "panels." + panelConfig.getName(),
                panelConfig.serialize(plugin.getPanelManager())
        ));
        return super.save();
    }

    public @Nullable PanelConfig getPanelConfigByName(String name) {
        return panelEntries.get(name);
    }

    public @Nullable PanelConfig removePanelConfigByName(String name) {
        return panelEntries.remove(name);
    }

    public void addPanelConfigByName(String name, PanelConfig panelConfig) {
        panelEntries.put(name, panelConfig);
    }

    public Set<String> getPanelNames() {
        return Collections.unmodifiableSet(panelEntries.keySet());
    }

}
