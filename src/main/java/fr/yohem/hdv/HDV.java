package fr.yohem.hdv;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class HDV extends JavaPlugin {
    private static Economy econ = null;
    List<HDVPlayer> hdvPlayers = new ArrayList<>();

    MenuManager menuManager = new MenuManager(this);

    public HDVPlayer findHdvPlayer(Player player){
        List<HDVPlayer> hdvPlayers = this.hdvPlayers.stream().filter(hdvP -> hdvP.getPlayer().equals(player)).collect(Collectors.toList());
        return hdvPlayers!=null&& !hdvPlayers.isEmpty()?hdvPlayers.get(0):null;
    }
    public HDVPlayer findHdvPlayer(String playerName){
        List<HDVPlayer> hdvPlayers = this.hdvPlayers.stream().filter(hdvP -> hdvP.getPlayer().getName().equals(playerName)).collect(Collectors.toList());
        return hdvPlayers!=null&& !hdvPlayers.isEmpty()?hdvPlayers.get(0):null;
    }

    @Override
    public void onEnable() {

        ConfigurationSerialization.registerClass(ItemSell.class);
        if (!setupEconomy() ) {
            System.out.println("PLUGIN HDV FAIL BEACAUSE VAULT NOT LOAD");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        System.out.println("HDV OPEN");
        menuManager.update(ItemSell.importItems(this));
        for (Player p : getServer().getOnlinePlayers()){
            hdvPlayers.add(new HDVPlayer(p));
        }

        /*LOAD YML for get hdvPlayers and Itemsells*/



        getCommand("hdv home").setExecutor(new CommandHDV(this));
        getCommand("hdv admin").setExecutor(new CommandHDV(this));
        getCommand("hdv whitelist").setExecutor(new CommandHDV(this));
        getServer().getPluginManager().registerEvents(new HDVListeners(this), this);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        for (ItemSell itemSell: menuManager.getItemsInHdv()){
            ItemSell.export(this, menuManager.getItemsInHdv());
        }
        System.out.println("HDV CLOSE");
        // Plugin shutdown logic
    }

    @Override
    public void  onLoad(){
        ConfigurationSerialization.registerClass(ItemSell.class);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }
}
