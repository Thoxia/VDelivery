package net.weesli.vdelivery;

import net.weesli.vdelivery.management.EventItem;
import net.weesli.vdelivery.management.EventPlayer;
import net.weesli.vdelivery.management.System;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class SaveDelivery extends BukkitRunnable {

    VDelivery main = VDelivery.getPlugin(VDelivery.class);
    System manager = new System();

    @Override
    public void run() {
        if (manager.isStart){
            for(Map.Entry<UUID, EventPlayer> x : new System().playerPoint.entrySet()){
                for(Map.Entry<String, EventItem> categories : new System().deliveryItem.entrySet()){
                    main.getConfig().set("saves.players." + x.getKey() + ".points." + categories.getValue().getId(), x.getValue().getPoint(categories.getValue().getId()));
                    main.getConfig().set("saves.categories." + categories.getKey(), categories.getValue().getId());
                    main.getConfig().set("saves.time", new System().task.getTime());
                    main.saveConfig();
                }
            }
        }else{
            this.cancel();
        }
    }
}
