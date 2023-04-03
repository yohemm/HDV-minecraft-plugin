package fr.yohem.hdv;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
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

            if (event.getView().getTitle().equals("§8 Hotel de ventes")) {
                int p = hdvPlayer.getPage();
                if (p<0){
                    hdvPlayer.menuRedirect("0",hdv);
                }
                if (currentItem.getType().equals(Material.STAINED_GLASS)) {

                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.SUIVANT.getDisplayName())){
                        player.sendMessage("Page suivante");
                        if (p <= hdv.menuManager.getMaxPageHdv())
                            hdvPlayer.setMenuStatus(p + 1 + "");
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.PRECENDANT.getDisplayName())){
                        player.sendMessage("Page Precendante");
                        if (p > 0)
                            hdvPlayer.setMenuStatus(p - 1 + "");
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.ACTUALISER.getDisplayName())){
                        player.sendMessage("Actualiser la page");
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_HDV.getDisplayName())){
                        player.sendMessage("Retour à l'hotel");
                        hdvPlayer.setMenuStatus("0");
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_EXPIRATION.getDisplayName())){
                        player.sendMessage("Voir les expirations");
                        hdvPlayer.setMenuStatus("expiration");
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_VENTES.getDisplayName())){
                        hdvPlayer.menuRedirect("mysell",hdv);
                    }
                }
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
//                    Renvoi sur menu confitmation
                    player.sendMessage("achat tenté sur " + iS.getItem().getType().name());
                    player.openInventory(hdv.menuManager.menuConfirmationAchat(iS));
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

                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.ACHETER.getDisplayName())){

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

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.RETOUR.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);

                    }
                }
                event.setCancelled(true);
                player.updateInventory();



            } else if (event.getView().getTitle().equals("§8 Mes Expirations")) {
                int p = hdvPlayer.getPage();
                if (p<0){
                    hdvPlayer.menuRedirect("expiration/0",hdv);
                    p=0;
                }
                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null)

                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.SUIVANT.getDisplayName())) {
                        if (p < hdv.menuManager.findItemExpiredOfSeller(player).size()/(MenuManager.MAX_ITEMSELL_PER_PAGE-1)+1)
                            hdvPlayer.setMenuStatus("expiration/"+(p + 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.PRECENDANT.getDisplayName())){
                        if (p > 0)
                            hdvPlayer.setMenuStatus("expiration/"+ (p - 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.ACTUALISER.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_HDV.getDisplayName())){
                        hdvPlayer.menuRedirect("0", hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_VENTES.getDisplayName())){
                        hdvPlayer.menuRedirect("mysell", hdv);

                    }
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                    player.openInventory(hdv.menuManager.menuConfirmationRecuperation(iS));
                }
                event.setCancelled(true);
                player.updateInventory();



            } else if (event.getView().getTitle().equals("§8 Hotel des Dragons Celeste")) {

                int p = hdvPlayer.getPage();
                if (p<0){
                    hdvPlayer.menuRedirect("admin/0",hdv);
                    p=0;
                }
                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null)
                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.SUIVANT.getDisplayName())) {
                        player.sendMessage("Page Suivante");
                        if (p < hdv.menuManager.findItemExpiredOfSeller(player).size()/(MenuManager.MAX_ITEMSELL_PER_PAGE-1)+1)
                            hdvPlayer.setMenuStatus("admin/"+(p + 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.PRECENDANT.getDisplayName())){
                        player.sendMessage("Page Precendante");
                        if (p > 0)
                            hdvPlayer.setMenuStatus("admin/"+ (p - 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.ACTUALISER.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_HDV.getDisplayName())){
                        hdvPlayer.menuRedirect("0", hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_VENTES.getDisplayName())){
                        hdvPlayer.menuRedirect("mysell", hdv);

                    }
                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                    player.sendMessage("Anulation de mise en vente tenté sur " + iS.getItem().getType().name());
                    player.openInventory(hdv.menuManager.menuConfirmationAnulation(iS));
                }
                event.setCancelled(true);
                player.updateInventory();



            } else if (event.getView().getTitle().equals("§8 Confirmation d'anulation de vente")) {
                ItemSell itemSell = hdv.menuManager.findItemSell(inv.getItem(9*2+4));
                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null)
                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.RETIRIRER.getDisplayName())) {
                        itemSell.setDate(new Date().getTime() - 604800000);
                        itemSell.getPlayer().getPlayer().sendMessage("Un de vos items en ventes viens d'être retirer du shop par un dragon celeste");
                        System.out.println(hdvPlayer.getMenuStatus());
                        hdvPlayer.menuBackRedirect(hdv);
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.RETOUR.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);
                    }else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.INSPECTER.getDisplayName())){
                        hdvPlayer.menuRedirect(itemSell.getPlayer().getPlayer().getName(),hdv);
                    }
                event.setCancelled(true);
                player.updateInventory();



            } else if (event.getView().getTitle().equals("§8 Confirmation récupération de vente")) {
                ItemSell itemSell = hdv.menuManager.findItemSell(inv.getItem(9*2+4));
                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null)
                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.RECUPERER.getDisplayName())) {
                        if(hdv.menuManager.getItemsInHdv().remove(itemSell)){
                            if (player.getInventory().addItem(currentItem).isEmpty())
                                itemSell.getPlayer().getPlayer().sendMessage("Vous venez de recuperer un de vos items");
                            else
                                itemSell.getPlayer().getPlayer().sendMessage("Impossible de recuperer un de vos items");
                            hdvPlayer.menuBackRedirect(hdv);
                        }
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.RETOUR.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);
                    }
                event.setCancelled(true);
                player.updateInventory();



            } else if (event.getView().getTitle().equals("§8 Mes ventes")){
                System.out.println("dans menu vente");
                String[] allString = hdvPlayer.getMenuStatus().split("/");

                int p = hdvPlayer.getPage();
                if (p<0){
                    hdvPlayer.menuRedirect(allString[0]+"/0",hdv);
                    p=0;
                }

                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null)
                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.SUIVANT.getDisplayName())) {
                        player.sendMessage("Page Suivante");
                        if (p < hdv.menuManager.findItemExpiredOfSeller(player).size()/(MenuManager.MAX_ITEMSELL_PER_PAGE-1)+1)
                            hdvPlayer.setMenuStatus(allString[0]+"/"+(p + 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.PRECENDANT.getDisplayName())){
                        player.sendMessage("Page Precendante");
                        if (p > 0)
                            hdvPlayer.setMenuStatus(allString[0]+"/"+ (p - 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.ACTUALISER.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_HDV.getDisplayName())){
                        hdvPlayer.menuRedirect("0", hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_EXPIRATION.getDisplayName())){
                        hdvPlayer.menuRedirect("expiration", hdv);

                    }

                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                    player.sendMessage("Récuperation ténté sur " + iS.getItem().getType().name());
                    player.openInventory(hdv.menuManager.menuConfirmationRecuperation(iS));
                }
                event.setCancelled(true);
                player.updateInventory();




            } else if (event.getView().getTitle().equals("§8 Inspection Joueur")){

                int p = hdvPlayer.getPage();
                if (p<0){
                    hdvPlayer.menuRedirect("mysell/0",hdv);
                    p=0;
                }

                if (currentItem.getItemMeta() != null && currentItem.getItemMeta().getDisplayName() != null)

                    if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.SUIVANT.getDisplayName())) {
                        player.sendMessage("Page Suivante");
                        if (p < hdv.menuManager.findItemExpiredOfSeller(player).size()/(MenuManager.MAX_ITEMSELL_PER_PAGE-1)+1)
                            hdvPlayer.setMenuStatus("mysell/"+(p + 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.PRECENDANT.getDisplayName())){
                        player.sendMessage("Page Precendante");
                        if (p > 0)
                            hdvPlayer.setMenuStatus("mysell/"+ (p - 1));
                        player.openInventory(hdv.menuManager.generateInv(hdvPlayer));
                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.ACTUALISER.getDisplayName())){
                        hdvPlayer.menuBackRedirect(hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_HDV.getDisplayName())){
                        hdvPlayer.menuRedirect("0", hdv);

                    } else if (currentItem.getItemMeta().getDisplayName().equals(MenuManager.ButtonAction.NAV_EXPIRATION.getDisplayName())){
                        hdvPlayer.menuRedirect("expiration", hdv);

                    }

                ItemSell iS = hdv.menuManager.findItemSell(currentItem);
                if (iS != null) {
                    // click sur item en Vente
                    player.sendMessage("Récuperation ténté sur " + iS.getItem().getType().name());
                    player.openInventory(hdv.menuManager.menuConfirmationAnulation(iS));
                }

                event.setCancelled(true);
                player.updateInventory();
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
