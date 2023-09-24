package fr.yohem.hdv;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HDVPlayer implements ConfigurationSerializable {
    private String menuStatus = "";//expired, 0, 1, article
    private UUID player;

    public String getMenuStatus() {
        return menuStatus;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }

    public void setMenuStatus(String menuStatus) {
        this.menuStatus = menuStatus;
    }

    public void menuRedirect(String menuStatus, HDV hdv){
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if ( p instanceof Player) {
            this.menuStatus = menuStatus;
            ((Player) p).openInventory(hdv.menuManager.generateInv(this));
        }
    }
    public void menuBackRedirect(HDV hdv){
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if ( p instanceof Player) {
            ((Player) p).openInventory(hdv.menuManager.generateInv(this));
        }
    }

    public HDVPlayer(OfflinePlayer player) {
        this.player = player.getUniqueId();
    }
    public int getPage(){
        String[] spliter = menuStatus.split("/");
        String page;
        if (spliter.length>1){
            page = spliter[1];
        }else
            page = spliter[0];
        try{

            return Integer.parseInt(page);
        }catch (NumberFormatException exception) {
            System.out.println("HDV ERROR PAGES");
            return -1;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serial = new HashMap<>();
        serial.put("player", player);
        return serial;
    }

    public static HDVPlayer deserialize(Map<String, Object> serial){
        return new HDVPlayer(Bukkit.getPlayer((UUID) serial.get("player")));
    }
}
