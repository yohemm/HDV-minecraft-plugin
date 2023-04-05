package fr.yohem.hdv;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.*;

public class ItemSell implements ConfigurationSerializable {
    final static long EXPIRATION_DELAY =604800000;
    private ItemStack item;
    private HDVPlayer player;
    private double price;
    private long date = new Date().getTime();

    public ItemSell(ItemStack item, HDVPlayer player, double price) {
        this.item = item;
        this.player = player;
        this.price = price;
    }

    public ItemSell(ItemStack item, HDVPlayer player, double price, long date) {
        this.item = item;
        this.player = player;
        this.price = price;
        this.date= date;
    }

    public long getDate() {
        return date;
    }

    public ItemStack getItem() {
        return item;
    }
    public boolean isExpired(){
        return new Date().getTime() - date > EXPIRATION_DELAY;
    }
    public ItemStack getItemWithDesc(){
        ItemStack it = item.clone();
        ItemMeta meta = it.getItemMeta();
        List<String> infos = meta.getLore()==null?new ArrayList<>():meta.getLore();
        long timeDiff = new Date().getTime() - date;
        if (!isExpired()){
            Duration duration = Duration.ofMillis(EXPIRATION_DELAY-timeDiff);
            long d = duration.toDays();
            long h = duration.toHours()%24;
            long m = duration.toMinutes()%60;
            infos.add("Vendeur : "+ player.getPlayer().getName());
            infos.add("Prix : "+ price + "$");
            infos.add("Expire dans : "+d+" jours, "+h+" heures et "+m+" minutes");
        }else {
            Duration duration = Duration.ofMillis(timeDiff-EXPIRATION_DELAY);
            long d = duration.toDays();
            long h = duration.toHours()%24;
            long m = duration.toMinutes()%60;
            infos.add("Expiré depuis : "+d+" jours, "+h+" heures et "+m+" minutes");
        }
        meta.setLore(infos);
        it.setItemMeta(meta);

        return it;
    }

    public HDVPlayer getPlayer() {
        return player;
    }

    public double getPrice() {
        return price;
    }

    public void setDate(long i) {
        date= i;
    }

    @Override
    public Map<String, Object> serialize() {
        Map serial = new HashMap<>();
        serial.put("item", item.serialize());
        serial.put("player", player.serialize());
        serial.put("price", price);
        serial.put("date", date);
        return null;
    }
    public static ItemSell deserialize(Map<String, Object> serial){
        return new ItemSell(ItemStack.deserialize((Map)serial.get("item")), HDVPlayer.deserialize((Map) serial.get("player")), (double) serial.get("price"), (long) serial.get("date"));
    }
}
