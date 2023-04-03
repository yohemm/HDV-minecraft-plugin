package fr.yohem.hdv;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandHDV implements CommandExecutor {
    HDV hdvPlug;
    public CommandHDV(HDV hdv_plug) {
        this.hdvPlug = hdv_plug;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            HDVPlayer hdvplayer;
            hdvplayer = hdvPlug.findHdvPlayer((Player) sender);
            if (hdvplayer != null) {
                Player player = hdvplayer.getPlayer();
                if (args.length >= 1) {
                    System.out.println(args[0]);
                    switch (args[0]) {
                        case "sell":
                            if (args.length == 2) {
                                int amout;
                                try {
                                    amout = Integer.parseInt(args[1]);

                                } catch (IllegalArgumentException exception) {
                                    player.sendMessage("format requi : /hdv sell <amout>, le nombre doit etre valide");
                                    return false;

                                }
                                if (amout < 1) {
                                    player.sendMessage("le prix doit positif!");
                                    return false;
                                }
                                ItemStack item = player.getInventory().getItemInMainHand();
                                if (item == null) {
                                    player.sendMessage("L'item que vous souhaitez mettre en vente n'est pas autoriser");
                                    return false;
                                }
                                hdvPlug.menuManager.addItemSellInHdv(new ItemSell(item, hdvplayer, amout));
                                player.sendMessage("Vous avez mis en ventes " + item.getAmount() + " " + item.getType().name() + " aux prix de " + amout);
                                return true;
                            } else {
                                player.sendMessage("format requi : /hdv sell <amout>");
                            }
                            break;
                        case "whitelist":
                            System.out.println("whitelist");
                            break;
                        case "admin":
                            System.out.println("admin");
                            break;
                        default:
                            for (Player p : hdvPlug.getServer().getOnlinePlayers()) {
                                if (p.getName().equals(args[1]))
                                    System.out.println("view");
                            }
                            break;
                    }
                } else {
                    System.out.println("open I,ventory");
                    player.openInventory(hdvPlug.menuManager.generateHdvInv(0));
                    player.updateInventory();
                }
            }
        }
        return false;
    }

}
