package net.weesli.vdelivery.management;

import org.bukkit.inventory.ItemStack;

public class EventItem {

    private String id;
    private ItemStack itemStack;

    private String category;
    private String delivery_type;


    public EventItem(String id, ItemStack itemStack, String  category, String delivery_type){this.id =id; this.category = category; this.itemStack = itemStack; this.delivery_type = delivery_type;}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDelivery_type() {
        return delivery_type;
    }

    public void setDelivery_type(String delivery_type) {
        this.delivery_type = delivery_type;
    }
}
