package fr.yohem.hdv;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommandHDV implements CommandExecutor {
    HDV hdvPlug;
    public CommandHDV(HDV hdv_plug) {
        this.hdvPlug = hdv_plug;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        System.out.println(cmd.getName());
        System.out.println(label);
        System.out.println(Arrays.asList(args));
        if (args.length >= 2 && args[0].toLowerCase().equals("open")) {
            if (args.length == 2) {
                if (!sender.hasPermission("hdv.commands.open")) {
                    sender.sendMessage("Vous n'avez pas access a cette commande");
                    return false;
                }
                for (Player pl : Bukkit.getOnlinePlayers())
                    if (pl.getName().equals(args[1])) {
                        System.out.println(pl.getName() + " Open HDV via inv");
                        hdvPlug.findHdvPlayer(pl).menuRedirect("0", hdvPlug);
                    }
                System.out.println("aaa");
            }
        }
        if (sender instanceof Player) {
            HDVPlayer hdvplayer;
            hdvplayer = hdvPlug.findHdvPlayer((Player) sender);
            if (hdvplayer != null) {
                Player player = (Player) hdvplayer.getPlayer();
                if (args.length >= 1) {
                    System.out.println(args[0]);
                    switch (args[0].toLowerCase()) {
                        case "help":
                            if (player.hasPermission("hdv.commands.help"))
                            sender.sendMessage("§m§n=============§r §6§lHotel des Ventes§r §m§n=============");
                            for (Map.Entry<String , List<String>> commandeH : getCommands().entrySet()){
                                if (player.hasPermission("hdv.commands."+commandeH.getKey())){
                                    player.sendMessage("/§6hdv "+commandeH.getValue().get(0)+ "§r- §a"+commandeH.getValue().get(1));
                                }
                            }

                            break;
                        case "sell":
                            if (args.length == 2) {
                                if (!player.hasPermission("hdv.commands.sell")){
                                    player.sendMessage("Vous n'avez pas access a cette commande");
                                    return false;
                                }
                                int amout;
                                try {
                                    amout = Integer.parseInt(args[1]);

                                } catch (IllegalArgumentException exception) {
                                    player.sendMessage("format requi : /hdv sell <amout>, le nombre doit être valide");
                                    return false;

                                }
                                if (amout < 1) {
                                    player.sendMessage("le prix doit positif!");
                                    return false;
                                }
                                ItemStack item = player.getInventory().getItemInMainHand();
                                if (item == null || hdvPlug.menuManager.getBlackList().contains(item.getType())) {
                                    player.sendMessage("L'item que vous souhaitez mettre en vente n'est pas autoriser");
                                    return false;
                                }
                                if (hdvPlug.menuManager.getBlackList().contains(item.getType())){
                                    player.sendMessage("Item Interdsit a la vente");
                                    return false;
                                }
                                player.getInventory().removeItem(item);
                                hdvPlug.menuManager.addItemSellInHdv(new ItemSell(item, player.getUniqueId(), amout));
                                player.sendMessage("Vous avez mis en ventes " + item.getAmount() + " " + item.getType().name() + " aux prix de " + amout);
                                return true;
                            } else {
                                player.sendMessage("format requi : /hdv sell <amout>");
                            }
                            break;
                        case "whitelist":
                            if (!player.hasPermission("hdv.commands.whitelist")){
                                player.sendMessage("Vous n'avez pas access a cette commande");
                                return false;
                            }
                            if (args.length == 2){
                                if (args[1].equalsIgnoreCase("add")){
                                    if (player.getInventory().getItemInMainHand() != null) {
                                        if (hdvPlug.menuManager.getBlackList().remove(player.getInventory().getItemInMainHand().getType()))
                                            player.sendMessage("Possibilté ajouté");
                                    }else
                                        player.sendMessage("Vous devez avoir un item dans votre main");

                                }else if(args[1].equalsIgnoreCase("rem")){
                                    if (player.getInventory().getItemInMainHand() != null) {
                                        if (hdvPlug.menuManager.getBlackList().add(player.getInventory().getItemInMainHand().getType())) {
                                            player.sendMessage("Possibilté supprimer");
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
                            System.out.println(hdvPlug.menuManager.getMaterialBlack());
                            break;
                        case "admin":
                            if (!player.hasPermission("hdv.commands.admin")){
                                player.sendMessage("Vous n'avez pas access a cette commande");
                                return false;
                            }
                            hdvplayer.setMenuStatus("admin");
                            player.openInventory(hdvPlug.menuManager.generateInv(hdvplayer));
                            break;
                        default:
                            if (!player.hasPermission("hdv.commands.see")){
                                player.sendMessage("Vous n'avez pas access a cette commande");
                                return false;
                            }
                            for (HDVPlayer p : hdvPlug.hdvPlayers) {
                                if (p.getPlayer().getName().equals(args[0])){
                                    hdvplayer.menuRedirect(p.getPlayer().getName(), hdvPlug);
                                }
                            }
                            break;
                    }
                } else {
                    if (!player.hasPermission("hdv.commands.use")){
                        player.sendMessage("Vous n'avez pas access a cette commande");
                        return false;
                    }
                    hdvplayer.setMenuStatus("0");
                    player.openInventory(hdvPlug.menuManager.generateInv(hdvplayer));
                    player.updateInventory();
                }
            }
        }
        return false;
    }

    public Map<String, List<String>> getCommands(){
        Map commands = new HashMap<>();
        commands.put("use", Arrays.asList("", "Acceder a l'hdv"));
        commands.put("sell", Arrays.asList("sell <price> ", "Vend l'item en main a un certain prix"));
        commands.put("help", Arrays.asList("help ", "Donne les infos sur les commandes de l'hdv"));
        commands.put("open", Arrays.asList("open <Player> ", "Force le joueur à ouvir l'hdv"));
        commands.put("see", Arrays.asList("<Player> ", "Inspecter les ventes du joueur"));
        commands.put("admin", Arrays.asList("admin ", "Ouvre la page des régulation de l'hdv"));
        commands.put("whitelist", Arrays.asList("whitelist <add | rem> ", "Force le joueur à ouvir l'hdv"));
        return commands;
    }

}
