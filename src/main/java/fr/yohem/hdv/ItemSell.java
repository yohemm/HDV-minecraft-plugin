package fr.yohem.hdv;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class ItemSell implements ConfigurationSerializable {
    final static long EXPIRATION_DELAY =604800000;
    private ItemStack item;
    private UUID player;
    private double price;
    private long date = new Date().getTime();

    public ItemSell(ItemStack item, UUID player, double price) {
        this.item = item;
        this.player = player;
        this.price = price;
    }

    public ItemSell(ItemStack item, UUID player, double price, long date) {
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
            infos.add("Vendeur : "+ Bukkit.getOfflinePlayer(player).getName());
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

    public UUID getPlayer() {
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
        serial.put("player", player.toString());
        serial.put("price", price);
        serial.put("date", date);
        return serial;
    }

    public static ItemSell deserialize(Map<String, Object> serial){
        return new ItemSell(ItemStack.deserialize((Map)serial.get("item")), UUID.fromString((String)serial.get("player")), (double) serial.get("price"), (long) serial.get("date"));
    }

    public static void export(HDV hdv,List<ItemSell> itemSells){
        final File file = new File(hdv.getDataFolder()+"/hdv.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("itemInHdv",itemSells);
        try {
            configuration.save(file);
        }catch (IOException e) {
            System.out.println("ITEM NOT EXPORT");
            throw new RuntimeException(e);
        }
    }

    public static List<ItemSell> importItems(HDV hdv){
        final File file = new File(hdv.getDataFolder()+"/hdv.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        return (List<ItemSell>) configuration.get("itemInHdv");
    }
}
