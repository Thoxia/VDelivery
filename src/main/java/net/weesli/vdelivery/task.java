package net.weesli.vdelivery;

import net.weesli.vdelivery.management.System;
import org.bukkit.scheduler.BukkitRunnable;

public class task extends BukkitRunnable {

    VDelivery main = VDelivery.getPlugin(VDelivery.class);
    System system = new System();

    private int time = 0;

    public task (int value){
        this.time = value;
    }

    @Override
    public void run() {
        if(time == 0){
            system.endEvent();
            this.cancel();
        }
        time--;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
