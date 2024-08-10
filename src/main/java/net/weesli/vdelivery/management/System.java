package net.weesli.vdelivery.management;

import me.clip.placeholderapi.PlaceholderAPI;
import net.weesli.vdelivery.Files.WebHookFile;
import net.weesli.vdelivery.SaveDelivery;
import net.weesli.vdelivery.VDelivery;
import net.weesli.vdelivery.hooks.BossBarHook;
import net.weesli.vdelivery.hooks.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.weesli.vdelivery.task;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class System {

    VDelivery main = VDelivery.getPlugin(VDelivery.class);


    public static HashMap<String, EventItem> deliveryItem = new HashMap<>();
    public static HashMap<UUID, EventPlayer> playerPoint = new HashMap<>();
    public static boolean isStart;
    public static task task;
    public static HashMap<String, List<LeaderBoard>> leaderBoard = new HashMap<>();
    BossBarHook bossbar = new BossBarHook();


    public Inventory getInventory(Player player){
        Inventory inventory = Bukkit.createInventory(null, main.getConfig().getInt("options.menu-settings.size"), ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("options.menu-settings.title")));
        int a = 0;
        for(Map.Entry<String, EventItem> x : deliveryItem.entrySet()){
            ItemStack itemStack = x.getValue().getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            for(String lores : main.getConfig().getStringList("options.delivery-type-lores." + getDeliveryTypeFromConfig(x.getValue().getId()))){
                lore.add(ChatColor.translateAlternateColorCodes('&',lores.replaceAll("%element%", ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items." + x.getValue().getId() + ".item-name"))).replaceAll("%count%", String.valueOf(playerPoint.get(player.getUniqueId()).getPoint(x.getValue().getCategory())))));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(main.getConfig().getIntegerList("options.menu-settings.item-slots").get(a), itemStack);
            a++;
        }
        if(main.getConfig().getBoolean("options.menu-settings.custom-items.enable")){
            for(String x : main.getConfig().getConfigurationSection("options.menu-settings.custom-items").getKeys(false)){
                if(x.equals("enable")){continue;}
                String path = "options.menu-settings.custom-items." + x;
                ItemStack itemStack  = new ItemStack(Material.getMaterial(main.getConfig().getString(path + ".item-type")));
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player,main.getConfig().getString(path + ".item-name"))));
                if(main.getConfig().getInt(path + ".custom-model-data") != 0){
                    meta.setCustomModelData(main.getConfig().getInt(path + ".custom-model-data"));
                }
                List<String> lore = new ArrayList<>();
                for (String lores : main.getConfig().getStringList(path + ".item-lore")){
                    lore.add(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player,lores)));
                }
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
                inventory.setItem(main.getConfig().getInt(path + ".slot"), itemStack);
            }
        }
        return inventory;
    }


    public ItemStack getItemFromConfig(String id){
        String path = "items." + id;
        ItemStack itemStack  = new ItemStack(Material.getMaterial(main.getConfig().getString(path + ".item-type")));
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(path + ".item-name")));
        if(main.getConfig().getInt(path + ".custom-model-data") != 0){
            meta.setCustomModelData(main.getConfig().getInt(path + ".custom-model-data"));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public String getCategoryFromConfig(String id){
        return main.getConfig().getString("items." + id + ".category");
    }

    public String getDeliveryTypeFromConfig(String id){
        return main.getConfig().getString("items." + id + ".delivery-type");
    }

    public List<String> getCategories(){
        List<String> categories = new ArrayList<>();
        for(String  x : main.getConfig().getConfigurationSection("categories").getKeys(false)){
            categories.add(x);
        }
        return categories;
    }


    public void startEvent(Integer value){
        setItemToCategory();
        isStart = true;
        for(Player player : Bukkit.getOnlinePlayers()){
            playerPoint.put(player.getUniqueId(), new EventPlayer(player.getUniqueId()));
        }
        task = new task(value);
        task.runTaskTimer(main,0,20);
        Bukkit.broadcastMessage(main.getPrefix() + ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("messages.broadcast-message-start")));
        if(WebHookFile.get().getBoolean("webhooks.enable")){
            sendEmbed();
        }
        main.getConfig().set("tops",null);
        main.saveConfig();
        if(main.getConfig().getBoolean("options.boss-bar.enabled")) bossbar.createBossBar();
        new SaveDelivery().runTaskTimerAsynchronously(main,0,1800);
    }

    public void endEvent(){
        isStart = false;
        if(main.getConfig().getBoolean("options.boss-bar.enabled")) bossbar.deleteBossBar();
        main.getConfig().set("saves", null);
        for(Map.Entry<String, EventItem> categories : deliveryItem.entrySet()){
            updateLeaderBoard(categories.getKey());
            if(!leaderBoard.get(categories.getKey()).isEmpty()){
                OfflinePlayer firstplayer = Bukkit.getOfflinePlayer(leaderBoard.get(categories.getKey()).get(0).getUuid());
                main.getConfig().set("tops." + categories.getKey() + ".first", firstplayer.getName());
            }
            if (leaderBoard.get(categories.getKey()).size() > 1){
                OfflinePlayer secplayer = Bukkit.getOfflinePlayer(leaderBoard.get(categories.getKey()).get(1).getUuid());
                main.getConfig().set("tops." + categories.getKey() + ".second", secplayer.getName());
            }
            if (leaderBoard.get(categories.getKey()).size() > 2){
                OfflinePlayer thirdplayer = Bukkit.getOfflinePlayer(leaderBoard.get(categories.getKey()).get(2).getUuid());
                main.getConfig().set("tops." + categories.getKey() + ".third", thirdplayer.getName());
            }
            main.saveConfig();
        }
        for(Map.Entry<UUID, EventPlayer> x : playerPoint.entrySet()){
            if(Bukkit.getPlayer(x.getKey()) == null){continue;}
            for(String category : main.getConfig().getConfigurationSection("tops").getKeys(false)){
                if(main.getConfig().get("rewards." + category) == null){continue;}
                if(main.getConfig().get("tops." + category + ".first") != null){
                    if (main.getConfig().getString("tops." + category + ".first").equalsIgnoreCase(Bukkit.getPlayer(x.getKey()).getName())){
                        for(String command : main.getConfig().getStringList("rewards." + category + ".first")){
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replaceAll("%player%", Bukkit.getPlayer(x.getKey()).getName()));
                        }
                        main.getConfig().set("tops." + category + ".first", null);
                        main.saveConfig();
                        continue;
                    }
                }
                if(main.getConfig().get("tops." + category + ".second") != null) {
                    if (main.getConfig().getString("tops." + category + ".second").equalsIgnoreCase(Bukkit.getPlayer(x.getKey()).getName())){
                        for (String command : main.getConfig().getStringList("rewards." + category + ".second")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", Bukkit.getPlayer(x.getKey()).getName()));
                        }
                        main.getConfig().set("tops." + category + ".second", null);
                        main.saveConfig();
                        continue;
                    }
                }
                if(main.getConfig().get("tops." + category + ".third") != null) {
                    if (main.getConfig().getString("tops." + category + ".third").equalsIgnoreCase(Bukkit.getPlayer(x.getKey()).getName())){
                        for (String command : main.getConfig().getStringList("rewards." + category + ".third")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", Bukkit.getPlayer(x.getKey()).getName()));
                        }
                        main.getConfig().set("tops." + category + ".third", null);
                        main.saveConfig();
                        continue;
                    }
                }
            }
        }
        deliveryItem.clear();
        playerPoint.clear();
        task.cancel();
        task = null;
        if(WebHookFile.get().getBoolean("webhooks.enable")){
            sendEndEmbed();
        }
        Bukkit.broadcastMessage(main.getPrefix() + ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("messages.broadcast-message-end")));
    }

    public void setItemToCategory(){
        List<String> categories = getCategories();
        Random random = new Random();
        for(int a = 0; a < 4; a++){
            String categoryName = categories.get(random.nextInt(categories.size()));
            categories.remove(categoryName);
            if(getItemsFromCategory(categoryName).size() == 0){continue;}
            String ItemID = getItemsFromCategory(categoryName).get(Math.abs(random.nextInt(getItemsFromCategory(categoryName).size())));
            deliveryItem.putIfAbsent(categoryName, new EventItem(
                    ItemID,
                    getItemFromConfig(ItemID),
                    categoryName,
                    getDeliveryTypeFromConfig(ItemID)
            ));
        }
    }

    public List<String> getItemsFromCategory(String category){
        List<String> elements = new ArrayList<>();
        for(String x : main.getConfig().getConfigurationSection("items").getKeys(false)){
            if(category.equalsIgnoreCase(main.getConfig().getString("items." + x + ".category"))){
                elements.add(x);
            }
        }
        return elements;
    }

    public class LeaderBoard implements Comparable<LeaderBoard>{

        private String category;
        private UUID uuid;
        private int point;

        public LeaderBoard(UUID uuid, int point, String category) {
            this.point = point;
            this.uuid = uuid;
            this.category = category;
        }


        @Override
        public int compareTo(@NotNull LeaderBoard o) {
            if(this.point > o.point){
                return 1;
            } else if (this.point < o.point) {
                return -1;
            }
            return 0;
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getPoint(){
                return point;
        }

    }


    public void updateLeaderBoard(String category) {
        if (leaderBoard.get(category) == null) {
            leaderBoard.put(category, new ArrayList<>());
        } else {
            leaderBoard.get(category).clear();
        }
        for (Map.Entry<UUID, EventPlayer> entry : playerPoint.entrySet()) {
            UUID playerId = entry.getKey();
            EventPlayer player = entry.getValue();
            int playerPoint = player.getPoint(category);
            leaderBoard.get(category).add(new LeaderBoard(playerId, playerPoint, category));
        }
        leaderBoard.get(category).sort(Collections.reverseOrder());
    }

    public void sendEndEmbed(){
        FileConfiguration file = WebHookFile.get();
        DiscordWebhook discordWebhook = new DiscordWebhook(file.getString("webhooks.webhook_url"));
        discordWebhook.setUsername(file.getString("webhooks.username"));
        discordWebhook.setAvatarUrl(file.getString("webhooks.avatar_url"));
        discordWebhook.setTts(true);
        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
        embedObject.setFooter("Powered By Weesli");
        embedObject.setColor(getColor(file.getString("webhooks.end-embed.color")));
        embedObject.setTitle(file.getString("webhooks.end-embed.title"));
        embedObject.setAuthor(file.getString("webhooks.end-embed.author.name"), file.getString("webhooks.end-embed.author.url"), file.getString("webhooks.end-embed.author.icon_url"));
        String desc = file.getString("webhooks.end-embed.description");
        embedObject.setDescription(desc);
        discordWebhook.addEmbed(embedObject);
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Webhook hatalı veya eksik lütfen dosyanızı kontrol edin!");

        }

    }

    public void sendEmbed(){
        FileConfiguration file = WebHookFile.get();
        DiscordWebhook discordWebhook = new DiscordWebhook(file.getString("webhooks.webhook_url"));
        discordWebhook.setUsername(file.getString("webhooks.username"));
        discordWebhook.setAvatarUrl(file.getString("webhooks.avatar_url"));
        discordWebhook.setTts(true);
        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
        embedObject.setFooter("Powered By Weesli");
        embedObject.setTitle(file.getString("webhooks.embeds.title"));
        embedObject.setAuthor(file.getString("webhooks.embeds.author.name"), file.getString("webhooks.embeds.author.url"), file.getString("webhooks.embeds.author.icon_url"));
        String desc = file.getString("webhooks.embeds.description");
        String value1 = "",value2 = "",value3 = "",value4 = "";
        String item1 = "", item2 = "", item3 = "", item4 = "";
        for(Map.Entry<String, EventItem> x : deliveryItem.entrySet()){
            if(value1.isEmpty()){
                value1 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("categories." + x.getValue().getCategory() + ".display-name")));
                item1 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items." + x.getValue().getId() + ".item-name")));
            }
            else if(value2.isEmpty()){
                value2 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("categories." + x.getValue().getCategory() + ".display-name")));
                item2 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items." + x.getValue().getId() + ".item-name")));
            }
            else if(value3.isEmpty()){
                value3 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("categories." + x.getValue().getCategory() + ".display-name")));
                item3 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items." + x.getValue().getId() + ".item-name")));
            }
            else if(value4.isEmpty()){
                value4 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("categories." + x.getValue().getCategory() + ".display-name")));
                item4 = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items." + x.getValue().getId() + ".item-name")));
            }
        }
        embedObject.setDescription(desc.replaceAll("%category-1%",value1).replaceAll("%category-2%", value2).replaceAll("%category-3%", value3).replaceAll("%category-4%", value4));
        for(String x : file.getConfigurationSection("webhooks.embeds.fields").getKeys(false)){
            embedObject.addField(file.getString("webhooks.embeds.fields." + x + ".name"), file.getString("webhooks.embeds.fields." + x + ".value").replaceAll("%item-1%", item1).replaceAll("%item-2%", item2).replaceAll("%item-3%", item3).replaceAll("%item-4%", item4), file.getBoolean("webhooks.embeds.fields." + x + ".inline"));
        }
        embedObject.setColor(getColor(file.getString("webhooks.embeds.color")));
        discordWebhook.addEmbed(embedObject);
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Webhook hatalı veya eksik lütfen dosyanızı kontrol edin!");
        }
    }


    public Color getColor(String name){
        HashMap<String, Color> colors = new HashMap<>();

        colors.put("BLACK", Color.BLACK);
        colors.put("WHITE", Color.WHITE);
        colors.put("RED", Color.RED);
        colors.put("GREEN", Color.GREEN);
        colors.put("BLUE", Color.BLUE);
        colors.put("YELLOW", Color.YELLOW);
        colors.put("ORANGE", Color.ORANGE);
        colors.put("PINK", Color.PINK);
        colors.put("MAGENTA", Color.MAGENTA);
        colors.put("CYAN", Color.CYAN);
        colors.put("GRAY", Color.GRAY);
        colors.put("DARK_GRAY", Color.DARK_GRAY);
        colors.put("LIGHT_GRAY", Color.LIGHT_GRAY);
        colors.put("PURPLE", new Color(128, 0, 128));
        colors.put("BROWN", new Color(165, 42, 42));

        return colors.get(name);
    }

    public String getTimeFormat(){
        int hour = task.getTime() / 3600;
        int minutes = (task.getTime() % 3600) / 60;
        int seconds = task.getTime() % 60;
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("options.time-format").replaceAll("%hour%", String.valueOf(hour)).replaceAll("%minute%", String.valueOf(minutes)).replaceAll("%sec%", String.valueOf(seconds)));
    }


}

