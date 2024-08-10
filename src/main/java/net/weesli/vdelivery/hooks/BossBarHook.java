package net.weesli.vdelivery.hooks;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import net.weesli.vdelivery.VDelivery;
import net.weesli.vdelivery.management.System;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;

public class BossBarHook implements Listener {

    BossBarManager manager = TabAPI.getInstance().getBossBarManager();


    public static BossBar bossBar;
    private static int startTime;


    public void createBossBar(){
        bossBar = manager.createBossBar("rdelivery",
                10,
                BarColor.valueOf(VDelivery.getPlugin(VDelivery.class).getConfig().getString("options.boss-bar.color")),
                BarStyle.valueOf(VDelivery.getPlugin(VDelivery.class).getConfig().getString("options.boss-bar.style")));
        bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', VDelivery.getPlugin(VDelivery.class).getConfig().getString("options.boss-bar.text")).replaceAll("%time%", new System().getTimeFormat()));
        for(Player player : Bukkit.getOnlinePlayers()){
            TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
            bossBar.addPlayer(tabPlayer);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if(bossBar == null){
                    this.cancel();
                }else{
                    updateBossBar();
                }
            }
        }.runTaskTimer(VDelivery.getPlugin(VDelivery.class),0,20);
        bossBar.setProgress(100);
        startTime = new System().task.getTime();
    }

    public void deleteBossBar(){
        if(bossBar != null){
            for(Player player : Bukkit.getOnlinePlayers()){
                TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
                bossBar.removePlayer(tabPlayer);
            }
            bossBar= null;
        }
    }

    public void updateBossBar(){
        if(bossBar != null){
            bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', VDelivery.getPlugin(VDelivery.class).getConfig().getString("options.boss-bar.text")).replaceAll("%time%", new System().getTimeFormat()));
        }
    }

    @EventHandler
    public void Join(PlayerJoinEvent e){
        if(bossBar != null){
            if(new System().isStart){
                Player player = e.getPlayer();
                new BukkitRunnable() {
                    int time = 1;
                    @Override
                    public void run() {
                        if(time == 0){
                            bossBar.addPlayer(TabAPI.getInstance().getPlayer(player.getUniqueId()));
                            this.cancel();
                        }
                        time--;
                    }
                }.runTaskTimer(VDelivery.getPlugin(VDelivery.class),0,10);

            }
        }
    }


}
