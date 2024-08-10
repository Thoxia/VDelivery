package net.weesli.vdelivery.events;

import net.weesli.vdelivery.VDelivery;
import net.weesli.vdelivery.management.EventItem;
import net.weesli.vdelivery.management.EventPlayer;
import net.weesli.vdelivery.management.System;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerListener implements Listener {

    VDelivery main = VDelivery.getPlugin(VDelivery.class);
    System manager = new System();

    @EventHandler
    public void Break(BlockBreakEvent e){
        if(!manager.isStart){return;}
        for(Map.Entry<String, EventItem> x : manager.deliveryItem.entrySet()){
            if(x.getValue().getDelivery_type().equalsIgnoreCase("break")){
                if(e.getBlock().getType() == x.getValue().getItemStack().getType()){
                    manager.playerPoint.get(e.getPlayer().getUniqueId()).addPoint(x.getValue().getCategory(), 1);
                }
            }
        }
    }

    @EventHandler
    public void Kill(EntityDeathEvent e){
        if(!manager.isStart){return;}
        if(e.getEntity().getKiller() instanceof Player){
            for(Map.Entry<String, EventItem> x : manager.deliveryItem.entrySet()){
                if(x.getValue().getDelivery_type().equalsIgnoreCase("kill")){
                    if(e.getEntityType() == EntityType.valueOf(main.getConfig().getString("items." + x.getValue().getId() + ".kill-enemy"))){
                        manager.playerPoint.get(e.getEntity().getKiller().getUniqueId()).addPoint(x.getValue().getCategory(), 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void Farming(BlockBreakEvent e) {
        if(!manager.isStart){return;}
        for(Map.Entry<String, EventItem> x : manager.deliveryItem.entrySet()){
            if(x.getValue().getDelivery_type().equalsIgnoreCase("farming")){
                if(Objects.equals(main.getConfig().getString("items." + x.getValue().getId() + ".farm-item"), e.getBlock().getType().name())){
                    final Ageable ageable = (Ageable) e.getBlock().getState().getBlockData();
                    if (ageable.getAge() == 7) {
                        manager.playerPoint.get(e.getPlayer().getUniqueId()).addPoint(x.getValue().getCategory(), 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void Craft(CraftItemEvent e){
        if(!manager.isStart){return;}
        if(e.getRecipe() == null){return;}
        for(Map.Entry<String, EventItem> x: manager.deliveryItem.entrySet()){
            if(Objects.requireNonNull(e.getRecipe()).getResult().getType().name().equals(x.getValue().getItemStack().getType().name())){
                manager.playerPoint.get(e.getViewers().get(0).getUniqueId()).addPoint(x.getValue().getCategory(),1);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if(main.getConfig().get("tops") != null){
            for(String x : main.getConfig().getConfigurationSection("tops").getKeys(false)){
                if(main.getConfig().getString("tops." + x + ".first") == null){
                    return;}
                if(main.getConfig().getString("tops." + x + ".first").equalsIgnoreCase(player.getName())){
                    for(String command : main.getConfig().getStringList("rewards." + x + ".first")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replaceAll("%player%", player.getName()));
                    }
                    main.getConfig().set("tops." + x + ".first", null);
                    main.saveConfig();
                    if(main.getConfig().getString("tops." + x + ".second") == null){
                        return;}
                }else if(main.getConfig().getString("tops." + x + ".second").equalsIgnoreCase(player.getName())) {
                    for (String command : main.getConfig().getStringList("rewards." + x + ".second")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", player.getName()));
                    }
                    main.getConfig().set("tops." + x + ".second", null);
                    main.saveConfig();
                    if(main.getConfig().getString("tops." + x + ".third") == null){
                        return;}
                }else if(main.getConfig().getString("tops." + x + ".third").equalsIgnoreCase(player.getName())) {
                    for (String command : main.getConfig().getStringList("rewards." + x + ".third")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", player.getName()));
                    }
                    main.getConfig().set("tops." + x + ".third", null);
                    main.saveConfig();
                }
            }
        }
        if(manager.isStart){
            if(manager.playerPoint.get(e.getPlayer().getUniqueId()) == null){
                manager.playerPoint.put(player.getUniqueId(), new EventPlayer(player.getUniqueId()));
            }
        }
    }
}
