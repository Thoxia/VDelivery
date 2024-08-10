package net.weesli.vdelivery.events;

import net.weesli.vdelivery.VDelivery;
import net.weesli.vdelivery.management.EventItem;
import net.weesli.vdelivery.management.System;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {

    VDelivery main = VDelivery.getPlugin(VDelivery.class);
    System system = new System();

    @EventHandler
    public void MenUClickEvent(InventoryClickEvent e){
        if(!system.isStart){return;}
        if(e.getCurrentItem() == null){return;}
        if (e.getView().getTitle().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("options.menu-settings.title")))) {
            e.setCancelled(true);
            if(e.getClickedInventory() != e.getView().getTopInventory()){return;}
            Player player = (Player) e.getWhoClicked();
            for(Map.Entry<String, EventItem> values : system.deliveryItem.entrySet()){
                if(values.getValue().getItemStack().getType() == e.getCurrentItem().getType()){
                    if(!values.getValue().getDelivery_type().equalsIgnoreCase("give")){continue;}
                    int amount = 0;
                    for(ItemStack itemStack : player.getInventory().getContents()){
                        if(itemStack == null){continue;}
                        if(itemStack.getType() == e.getCurrentItem().getType()){
                            if(itemStack.getItemMeta().hasCustomModelData() || e.getCurrentItem().getItemMeta().hasCustomModelData()){
                                if(itemStack.getItemMeta().getCustomModelData() == e.getCurrentItem().getItemMeta().getCustomModelData()){
                                    amount += itemStack.getAmount();
                                }
                            }else{
                                amount += itemStack.getAmount();
                            }
                        }
                    }
                    if(amount == 0){
                        player.sendMessage(main.getPrefix() + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.no-amount")));
                        return;
                    }
                    ItemStack itemStack = new ItemStack(Material.getMaterial(e.getCurrentItem().getType().name()));
                    ItemMeta meta = itemStack.getItemMeta();
                    if(meta.hasCustomModelData()){
                        meta.setCustomModelData(e.getCurrentItem().getItemMeta().getCustomModelData());
                    }
                    itemStack.setItemMeta(meta);
                    itemStack.setAmount(amount);
                    player.getInventory().removeItemAnySlot(itemStack);
                    system.playerPoint.get(player.getUniqueId()).addPoint(values.getValue().getCategory(), amount);
                    player.closeInventory();
                    player.openInventory(system.getInventory(player));
                    player.sendMessage(main.getPrefix() + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.give-item").replaceAll("%amount%", String.valueOf(amount))));

                }
            }
        }
    }



}
