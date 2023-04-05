package fr.yohem.hdv;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HDVPlayer implements ConfigurationSerializable {
    private String menuStatus = "";//expired, 0, 1, article
    private Player player;

    public String getMenuStatus() {
        return menuStatus;
    }

    public Player getPlayer() {
        return player;
    }

    public void setMenuStatus(String menuStatus) {
        this.menuStatus = menuStatus;
    }

    public void menuRedirect(String menuStatus, HDV hdv){
        this.menuStatus = menuStatus;
        player.openInventory(hdv.menuManager.generateInv(this));
    }
    public void menuBackRedirect(HDV hdv){
        player.openInventory(hdv.menuManager.generateInv(this));
    }

    public HDVPlayer(Player player) {
        this.player = player;
    }
    public int getPage(){
        String[] spliter = menuStatus.split("/");
        String page;
        if (spliter.length>1){
            page = spliter[1];
        }else
            page = spliter[0];
        try{
            return Integer.parseInt(menuStatus);
        }catch (NumberFormatException exception) {
            return -1;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serial = new HashMap<>();
        serial.put("player", player.getUniqueId());
        return serial;
    }

    public static HDVPlayer deserialize(Map<String, Object> serial){
        return new HDVPlayer(Bukkit.getPlayer((UUID) serial.get("player")));
    }
}
