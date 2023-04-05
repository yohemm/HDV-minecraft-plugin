package fr.yohem.hdv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
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
                                if (hdvPlug.menuManager.getBlackList().contains(item.getType())){
                                    player.sendMessage("Item Interdsit a la vente");
                                    return false;
                                }
                                player.getInventory().remove(item);
                                hdvPlug.menuManager.addItemSellInHdv(new ItemSell(item, hdvplayer, amout));
                                player.sendMessage("Vous avez mis en ventes " + item.getAmount() + " " + item.getType().name() + " aux prix de " + amout);
                                return true;
                            } else {
                                player.sendMessage("format requi : /hdv sell <amout>");
                            }
                            break;
                        case "whitelist":
                            System.out.println("whitelist");
                            if (args.length == 2){
                                if (args[1].equalsIgnoreCase("add")){
                                    if (player.getInventory().getItemInMainHand() != null) {
                                        if (hdvPlug.menuManager.getBlackList().remove(player.getInventory().getItemInMainHand().getType()))
                                            player.sendMessage("Action effectuer");
                                    }else
                                        player.sendMessage("Vous devez avoir un item dans votre main");

                                }else if(args[1].equalsIgnoreCase("rem")){
                                    if (player.getInventory().getItemInMainHand() != null) {
                                        if (hdvPlug.menuManager.getBlackList().add(player.getInventory().getItemInMainHand().getType())) {
                                            player.sendMessage("Action effectuer");
                                            List<ItemSell> itemSellList = hdvPlug.menuManager.getItemsInHdv();
                                            for (ItemSell itemSell : hdvPlug.menuManager.getItemsInHdv())
                                                if (!itemSell.isExpired() && hdvPlug.menuManager.getBlackList().contains(itemSell.getItem().getType())){
                                                    itemSell.setDate(new Date().getTime()-ItemSell.EXPIRATION_DELAY);
                                                }
                                        }
                                    }else
                                        player.sendMessage("Vous devez avoir un item dans votre main");
                                }else {
                                    player.sendMessage("Mauvaise utilisation : /hdv whitelist [rem/add]");
                                }
                            }
                            break;
                        case "admin":
                            hdvplayer.setMenuStatus("admin");
                            player.openInventory(hdvPlug.menuManager.generateInv(hdvplayer));
                            break;
                        default:
                            for (HDVPlayer p : hdvPlug.hdvPlayers) {
                                if (p.getPlayer().getName().equals(args[0])){
                                    System.out.println("view");
                                    hdvplayer.menuRedirect(p.getPlayer().getName(), hdvPlug);
                                }
                            }
                            break;
                    }
                } else {
                    hdvplayer.setMenuStatus("0");
                    player.openInventory(hdvPlug.menuManager.generateInv(hdvplayer));
                    player.updateInventory();
                }
            }
        }
        return false;
    }

}
