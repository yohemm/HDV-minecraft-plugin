package fr.yohem.hdv;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HDVListeners implements Listener {
    private HDV hdv;
    public HDVListeners(HDV plugin) {
        System.out.println("a");
        this.hdv = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        HDVPlayer hdvPlayer = hdv.findHdvPlayer(player);
        if (hdvPlayer == null){
            hdv.hdvPlayers.add(new HDVPlayer(player));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        HDVPlayer hdvPlayer = hdv.findHdvPlayer(player);
        int p = hdvPlayer.getPage();
            if (event.getView().getTitle().equals("§8 Hotel de ventes")) {

                if (currentItem.getType().equals(Material.STAINED_GLASS)) {
                    switch (currentItem.getItemMeta().getDisplayName()) {
                        case "Page Suivante":
                            player.sendMessage("Page suivante");
                            if (p < hdv.menuManager.getMaxPageHdv())
                                hdvPlayer.setMenuStatus(p + 1 + "");
                            player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                            break;
                        case "Page Precendante":
                            player.sendMessage("Page Precendante");
                            if (p > 0)
                                hdvPlayer.setMenuStatus(p - 1 + "");
                            player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                            break;
                        case "Actualiser la page":
                            player.sendMessage("Actualiser la page");
                            player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                            break;
                        case "Retour à l'hotel":
                            player.sendMessage("Retour à l'hotel");
                            hdvPlayer.setMenuStatus("0");
                            player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                            break;
                        case "Voir les expirations":
                            player.sendMessage("Voir les expirations");
                            hdvPlayer.setMenuStatus("expiration");
                            player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                            break;
                        case "A propos de l'HDV":
                            player.sendMessage("A propos de l'HDV");
                            break;
                        default:
                            break;
                    }
                }
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
//                    Renvoi sur menu confitmation
                    player.sendMessage("achat tenté sur " + iS.getItem().getType().name());
                    player.openInventory(hdv.menuManager.menuConfirmation(iS));
                }
                event.setCancelled(true);
                player.updateInventory();


            } else if (event.getView().getTitle().equals("§8 Confirmation d'achat")) {
                ItemSell itemSell = hdv.menuManager.findItemSell(inv.getItem(9*2+4));
                if (itemSell == null){
                    player.sendMessage("Erreur lors de la selection d'article");
                    hdvPlayer.menuRedirect("0",hdv);
                }
                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null) {
                    if (currentItem.getItemMeta().getDisplayName().equals("Acheter")) {

                        if (hdvPlayer.amout >= itemSell.getPrice()) {
//!                    Cas ou l'on a l'inv plein
                            List<ItemSell> itemInHdv = hdv.menuManager.getItemsInHdv();
                            if (!player.getInventory().addItem(itemSell.getItem()).isEmpty()) {
                                player.sendMessage("Error Inventaire Full");
                                return;
                            }
                            if(!itemInHdv.remove(itemSell)) {
                                player.sendMessage("Pas de remove dans l'hdv");
                                return;
                            }
                            hdv.menuManager.setItemsInHdv(itemInHdv);

                            hdvPlayer.amout -= itemSell.getPrice();
                            itemSell.getPlayer().amout += itemSell.getPrice();
                            hdvPlayer.menuRedirect("0", hdv);
                        }else {
                            player.sendMessage("Vous n'avez pas assez de Berry!");
                        }

                    } else if (currentItem.getItemMeta().getDisplayName().equals("Anuler")) {
                        hdvPlayer.menuRedirect("0",hdv);
                    }
                }
                event.setCancelled(true);
                player.updateInventory();


            } else if (event.getView().getTitle().equals("§8 Mes Expirations")) {
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                }
                event.setCancelled(true);
                player.updateInventory();


            } else if (event.getView().getTitle().equals("§8 L'Hotel des Dragons Celeste")) {
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                }
                event.setCancelled(true);
                player.updateInventory();


            } else if (event.getView().getTitle().equals("§8 Confirmation d'anulation de vente")) {
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                }
                event.setCancelled(true);
                player.updateInventory();


            }
        }
    }

    @EventHandler
    public void onDrag(InventoryInteractEvent event){
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        System.out.println(event.getEventName());
        player.sendMessage("drag");
        if(event.getView().getTitle().equals("§8 Hotel de ventes")){
            event.setCancelled(true);
            System.out.println("drag hdv");
        }

    }
}
