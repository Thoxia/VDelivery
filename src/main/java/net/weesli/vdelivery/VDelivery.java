package net.weesli.vdelivery;

import net.weesli.vdelivery.Files.WebHookFile;
import net.weesli.vdelivery.events.GUIListener;
import net.weesli.vdelivery.events.PlayerListener;
import net.weesli.vdelivery.hooks.BossBarHook;
import net.weesli.vdelivery.hooks.papiHook;
import net.weesli.vdelivery.management.EventItem;
import net.weesli.vdelivery.management.EventPlayer;
import net.weesli.vdelivery.management.System;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public final class VDelivery extends JavaPlugin {

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "PlaceHolderAPI başarılı bir şekilde kuruldu");
            new papiHook().register();
        }
        saveDefaultConfig();
        saveConfig();
        saveResource("webhooks.yml", false);
        WebHookFile.setup();
        WebHookFile.get().options().copyDefaults(true);
        WebHookFile.save();
        getCommand("vdelivery").setExecutor(new commands());
        getCommand("vdelivery").setTabCompleter(new commands());
        getServer().getPluginManager().registerEvents(new PlayerListener(),this);
        getServer().getPluginManager().registerEvents(new GUIListener(),this);
        getServer().getPluginManager().registerEvents(new BossBarHook(),this);
        if(getConfig().get("saves") != null) {
            System manager = new System();
            manager.isStart = true;
            for (String x : getConfig().getConfigurationSection("saves.players").getKeys(false)) {
                manager.playerPoint.putIfAbsent(UUID.fromString(x), new EventPlayer(UUID.fromString(x)));
                for (String value : getConfig().getConfigurationSection("saves.players." + x + ".points").getKeys(false)) {
                    manager.playerPoint.get(UUID.fromString(x)).addPoint(value, getConfig().getInt("saves.players." + x + ".points." + value));
                }
            }
            for (String categories : getConfig().getConfigurationSection("saves.categories").getKeys(false)) {
                manager.deliveryItem.putIfAbsent(categories, new EventItem(
                        getConfig().getString("saves.categories." + categories),
                        manager.getItemFromConfig(getConfig().getString("saves.categories." + categories)),
                        categories,
                        manager.getDeliveryTypeFromConfig(getConfig().getString("saves.categories." + categories))
                ));
            }
            manager.task = new task(getConfig().getInt("saves.time"));
            manager.task.runTaskTimer(this, 0, 20);
            if (getConfig().get("options.boss-bar") == null) {
                getConfig().set("options.boss-bar.enbaled", false);
                getConfig().set("options.boss-bar.color", "RED");
                getConfig().set("options.boss-bar.style", "PROGRESS");
                getConfig().set("options.boss-bar.text", "&eTeslimet etkinliği: &c%time%");
                saveConfig();
            }
        }
    }

    @Override
    public void onDisable() {
        for(Map.Entry<UUID, EventPlayer> x : new System().playerPoint.entrySet()){
            for(Map.Entry<String, EventItem> categories : new System().deliveryItem.entrySet()){
                getConfig().set("saves.players." + x.getKey() + ".points." + categories.getValue().getId(), x.getValue().getPoint(categories.getValue().getId()));
                getConfig().set("saves.categories." + categories.getKey(), categories.getValue().getId());
                getConfig().set("saves.time", new System().task.getTime());
                saveConfig();
            }
        }
    }

    public String getPrefix(){
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("options.prefix"));
    }


}
