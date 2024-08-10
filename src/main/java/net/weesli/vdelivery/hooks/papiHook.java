package net.weesli.vdelivery.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.weesli.vdelivery.VDelivery;
import net.weesli.vdelivery.management.EventItem;
import net.weesli.vdelivery.management.System;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class papiHook extends PlaceholderExpansion {

    System manager = new System();
    VDelivery main = VDelivery.getPlugin(VDelivery.class);

    @Override
    public @NotNull String getIdentifier() {
        return "vdelivery";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Weesli";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    Pattern matcher = Pattern.compile("([A-Z-a-z-0-9]+)_point");
    Pattern topMatcher = Pattern.compile("([0-9]+)_top_([a-z-A-Z-0-9]+)_([a-z]+)");


    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(params.endsWith("point")){
            Matcher m = matcher.matcher(params);
            if(m.find()){
                String x = m.group(1);
                if(manager.playerPoint.get(player.getUniqueId()) != null){
                    return String.valueOf(manager.playerPoint.get(player.getUniqueId()).getPoint(x));
                }else{
                    return "";
                }
            }
        }
        if(params.equalsIgnoreCase("time")){
            if(manager.isStart){
                int hour = manager.task.getTime() / 3600;
                int minutes = (manager.task.getTime() % 3600) / 60;
                int seconds = manager.task.getTime() % 60;
                return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("options.time-format").replaceAll("%hour%", String.valueOf(hour)).replaceAll("%minute%", String.valueOf(minutes)).replaceAll("%sec%", String.valueOf(seconds)));
            }else{
                return "";
            }
        }
        if(params.contains("_top_")){
            if(!manager.isStart){return "#";}
            Matcher m = topMatcher.matcher(params);
            if(m.find()){
                String number = m.group(1);
                String category = m.group(2);
                String values = m.group(3);
                manager.updateLeaderBoard(category);
                if(manager.leaderBoard.get(category).size() < Integer.parseInt(number)){
                    return "";
                }
                if(values.equals("n")){
                    Player target = Bukkit.getPlayer(manager.leaderBoard.get(category).get(Integer.parseInt(number) - 1).getUuid());
                    if(target == null){
                        return Bukkit.getOfflinePlayer(manager.leaderBoard.get(category).get(Integer.parseInt(number) - 1).getUuid()).getName();
                    }else{
                        return target.getName();
                    }
                }
                if(values.equals("p")){
                    return String.valueOf(manager.leaderBoard.get(category).get(Integer.parseInt(number) - 1).getPoint());
                }
            }
        }

        return "";
    }
}
