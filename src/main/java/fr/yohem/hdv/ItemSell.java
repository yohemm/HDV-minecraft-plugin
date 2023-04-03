package fr.yohem.hdv;

import jdk.jfr.Timestamp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ItemSell {
    private ItemStack item;
    private HDVPlayer player;
    private double price;
    private long date = new Date().getTime();

    public ItemSell(ItemStack item, HDVPlayer player, double price) {
        this.item = item;
        this.player = player;
        this.price = price;
    }

    public long getDate() {
        return date;
    }

    public ItemStack getItem() {
        return item;
    }
    public boolean isExpired(){
        return new Date().getTime() - date > 604800000;
    }
    public ItemStack getItemWithDesc(){
        ItemStack it = item.clone();
        ItemMeta meta = it.getItemMeta();
        List<String> infos = meta.getLore()==null?new ArrayList<>():meta.getLore();
        long timeDiff = new Date().getTime() - date;
        if (!isExpired()){
            Duration duration = Duration.ofMillis(604800000-timeDiff);
            long d = duration.toDays();
            long h = duration.toHours()%24;
            long m = duration.toMinutes()%60;
            infos.add("Vendeur : "+ player.getPlayer().getName());
            infos.add("Prix : "+ price + "$");
            infos.add("Expire dans : "+d+" jours, "+h+" heures et "+m+" minutes");
        }else {
            Duration duration = Duration.ofMillis(timeDiff-604800000);
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
}
