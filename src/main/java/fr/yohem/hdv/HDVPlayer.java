package fr.yohem.hdv;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public class HDVPlayer {
    private String menuStatus = "";//expired, 0, 1, article
    int amout = 500;
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
}
