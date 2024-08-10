package net.weesli.vdelivery.management;

import java.util.HashMap;
import java.util.UUID;

public class EventPlayer {

    UUID player;

    HashMap<String, Integer> points = new HashMap<>();

    public EventPlayer(UUID player){
        this.player = player;
    }

    public void addPoint(String value, int amount){
        points.putIfAbsent(value, 0);
        points.replace(value, getPoint(value) + amount);
    }

    public Integer getPoint(String value){
        points.putIfAbsent(value,0);
        return points.get(value);
    }




}
