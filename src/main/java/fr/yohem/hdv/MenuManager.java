package fr.yohem.hdv;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MenuManager {
    private List<ItemSell> itemsInHdv = new ArrayList<>();
    private HDV hdvPlug;

    public void addItemSellInHdv(ItemSell itemSell){
        itemsInHdv.add(itemSell);
    }
    public void remItemSellInHdv(ItemSell itemSell){
        itemsInHdv.remove(itemSell);
    }

    public List<ItemSell> getItemsInHdv() {
        return new ArrayList<>(itemsInHdv);
    }

    public void setItemsInHdv(List<ItemSell> itemsInHdv) {
        this.itemsInHdv = itemsInHdv;
    }

    public ItemSell findItemSell(ItemStack itemWithDesc) {
        if (itemWithDesc == null) return null;
        for (ItemSell iS : getItemsInHdv())
            if (itemWithDesc.equals(iS.getItemWithDesc())) {
                return iS;
            }
        return null;
    }

    public MenuManager(HDV hdvPlug) {
        this.hdvPlug=hdvPlug;
    }

    public List<ItemSell> getItemSellable(){
        List<ItemSell> itemSells = itemsInHdv.stream().filter(iS -> new Date().getTime() - iS.getDate() < 604800000).collect(Collectors.toList());
        Collections.reverse(itemSells);
        return itemSells;
    }

    public List<ItemSell> findItemOfSeller(Player seller){
        return itemsInHdv.stream().filter(iS -> iS.getPlayer()==seller).collect(Collectors.toList());
    }
    public List<ItemSell> findItemExpiredOfSeller(Player seller){
        return itemsInHdv.stream().filter(iS -> iS.getPlayer()==seller && new Date().getTime() - iS.getDate() > 604800000).collect(Collectors.toList());
    }

    public Inventory menuConfirmation(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation d'achat");
        inv.setItem(9*2+4, itemSell.getItemWithDesc());
        inv.setItem(9*2+2, new ItemGenerator(Material.GLASS).setName("Acheter").generate());
        inv.setItem(9*2+1, new ItemGenerator(Material.GLASS).setName("Acheter").generate());
        inv.setItem(9*3+2, new ItemGenerator(Material.GLASS).setName("Acheter").generate());
        inv.setItem(9*3+1, new ItemGenerator(Material.GLASS).setName("Acheter").generate());
        inv.setItem(9+2, new ItemGenerator(Material.GLASS).setName("Acheter").generate());
        inv.setItem(9+1, new ItemGenerator(Material.GLASS).setName("Acheter").generate());

        inv.setItem(9*2+6, new ItemGenerator(Material.GLASS).setName("Anuler").generate());
        inv.setItem(9*2+7, new ItemGenerator(Material.GLASS).setName("Anuler").generate());
        inv.setItem(9*3+6, new ItemGenerator(Material.GLASS).setName("Anuler").generate());
        inv.setItem(9*3+7, new ItemGenerator(Material.GLASS).setName("Anuler").generate());
        inv.setItem(9+6, new ItemGenerator(Material.GLASS).setName("Anuler").generate());
        inv.setItem(9+7, new ItemGenerator(Material.GLASS).setName("Anuler").generate());

        inv.setItem(9*4, new ItemGenerator(Material.BARRIER).setName("Anuler").generate());

        return inv;
    }

    public Inventory generateExpirationInv(HDVPlayer hdvPlayer){
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Mes Expirations");

        return inv;
    }

    public Inventory generateInv(HDVPlayer hdvPlayer){
        if (hdvPlayer.getMenuStatus().equals("expiration")){
            return  generateExpirationInv(hdvPlayer);
        } else {
            // manque Syteme de confirmation Article a geré ici


            try{
                return generateHdvInv(Integer.parseInt(hdvPlayer.getMenuStatus()));
            }catch (NumberFormatException exception){
                hdvPlayer.getPlayer().sendMessage("menuStatus non trouvé");
                hdvPlayer.setMenuStatus("0");
                return generateHdvInv(0);
            }

        }

    }

    public Inventory generateHdvInv(int page){
        int startId = 28*page;
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Hotel de ventes");
        inv.setItem(50, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName("Page Suivante").generate());
        inv.setItem(48, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName("Page Precendante").generate());
        inv.setItem(49, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName("Actualiser la page").generate());
        inv.setItem(7, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName("Retour à l'hotel").generate());
        inv.setItem(8, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName("Voir les expirations").generate());
        List<String> infos = new ArrayList<>();
        infos.add("L'hotel de ventes est accessible uniquement via le courtier");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName("A propos de l'HDV").setLore(infos).generate());
        List<ItemSell>itemSellable = getItemSellable();

        int itemSellPos = 10;
        for(int j = 0; j<4; j++){
            for(int i = 0; i<7; i++){
                int id = j*7+i+startId;
                if (itemSellable.size()>id && itemSellable.get(id) != null){
                    inv.setItem(itemSellPos+j*7+i, itemSellable.get(id).getItemWithDesc());
                }

            }
            itemSellPos+=2;

        }
        return inv;

    }

    public int getMaxPageHdv() {
        return getItemSellable().size()/27;
    }
}
