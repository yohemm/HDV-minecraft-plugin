package fr.yohem.hdv;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class HDV extends JavaPlugin {
    List<HDVPlayer> hdvPlayers = new ArrayList<>();

    MenuManager menuManager = new MenuManager(this);

    public HDVPlayer findHdvPlayer(Player player){
        List<HDVPlayer> hdvPlayers = this.hdvPlayers.stream().filter(hdvP -> hdvP.getPlayer().equals(player)).collect(Collectors.toList());
        return hdvPlayers!=null&& !hdvPlayers.isEmpty()?hdvPlayers.get(0):null;
    }

    @Override
    public void onEnable() {
        System.out.println("HDV OPEN");
        for (Player p : getServer().getOnlinePlayers()){
            hdvPlayers.add(new HDVPlayer(p));
        }
        getCommand("hdv").setExecutor(new CommandHDV(this));
        getServer().getPluginManager().registerEvents(new HDVListeners(this), this);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        System.out.println("HDV CLOSE");
        // Plugin shutdown logic
    }
}
