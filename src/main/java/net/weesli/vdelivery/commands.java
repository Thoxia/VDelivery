package net.weesli.vdelivery;

import com.google.common.base.Charsets;
import net.weesli.vdelivery.Files.WebHookFile;
import net.weesli.vdelivery.management.System;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class commands implements CommandExecutor, TabCompleter {

    VDelivery main = VDelivery.getPlugin(VDelivery.class);
    System management = new System();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            if(args.length == 0){
                player.sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + "VDelivery - By Weesli" + ChatColor.GOLD + "]");
                player.sendMessage(ChatColor.YELLOW + "/vdelivery open - Teslimat menusunu açar");
                player.sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + "VDelivery - By Weesli" + ChatColor.GOLD + "]");
            } else if (args[0].equalsIgnoreCase("open")) {
                if(management.isStart){
                    player.openInventory(management.getInventory(player));
                }else{
                    player.sendMessage(main.getPrefix() + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.no-start-event")));
                }
            }
        }
        if(args.length == 0){

        }else if(args[0].equalsIgnoreCase("start")){
            if(commandSender.hasPermission("vdelivery.start")){
                if(args.length > 1){
                    if(!management.isStart){
                        int value = 0;
                        if(args[1].endsWith("d")){
                            int x = Integer.parseInt(args[1].replaceAll("d", ""));
                            int hour = (x * 24);
                            int minute = (hour * 60);
                            int sec = (minute * 60);
                            value = sec;
                        }else if(args[1].endsWith("m")){
                            int x = Integer.parseInt(args[1].replaceAll("m", ""));
                            int sec = (x * 60);
                            value = sec;
                        } else if (args[1].endsWith("s")) {
                            int x = Integer.parseInt(args[1].replaceAll("s", ""));
                            value = x;
                        } else if (args[1].endsWith("h")) {
                            int x = Integer.parseInt(args[1].replaceAll("h", ""));
                            int minute = (x * 60);
                            int sec = (minute * 60);
                            value = sec;
                        }
                        management.startEvent(value);
                    }
                }else{
                    commandSender.sendMessage(ChatColor.RED + "Bir süre belirleyin");
                }
            }
        } else if (args[0].equalsIgnoreCase("end")) {
            if(commandSender.hasPermission("vdelivery.admin")){
                if(management.isStart){
                    management.endEvent();
                }
            }
        }
        else if(args[0].equalsIgnoreCase("reload")){
            if(commandSender.hasPermission("vdelivery.admin")){
                main.reloadConfig();
                main.saveConfig();
                WebHookFile.reloadConfig();
                commandSender.sendMessage(ChatColor.GREEN + "Yapılandırma dosyaları yenilendi");
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> args = new ArrayList<>();
        if(commandSender.isOp()){
            args.add("reload");
            args.add("start");
            args.add("end");
        }
        args.add("open");
        if(strings[0].equalsIgnoreCase("start")){
            List<String> time = new ArrayList<>();
            time.add("30d");
            time.add("24h");
            time.add("15m");
            time.add("15s");
            return time;
        }
        return args;
    }
}
