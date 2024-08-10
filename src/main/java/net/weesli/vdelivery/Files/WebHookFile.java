package net.weesli.vdelivery.Files;

import net.weesli.vdelivery.VDelivery;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class WebHookFile {


    private static File file;
    private static FileConfiguration filemanager;

    public static void setup(){
        file = new File(VDelivery.getPlugin(VDelivery.class).getDataFolder(), "webhooks.yml");
        if(file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        filemanager = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return filemanager;
    }

    public static  void save(){
        try {
            filemanager.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadConfig() {
        File file = new File(VDelivery.getPlugin(VDelivery.class).getDataFolder(), "webhooks.yml");
        filemanager = YamlConfiguration.loadConfiguration(file);
    }
}
