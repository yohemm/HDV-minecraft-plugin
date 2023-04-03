package fr.yohem.hdv;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class MenuManager {
    public enum ButtonAction {
        ACHETER("Acheter"),
        SUIVANT("Page Suivante"),
        PRECENDANT("Page Precendante"),
        ACTUALISER("Actualiser la page"),
        NAV_HDV("Retour à l'hotel des ventes"),
        NAV_VENTES("Voir mes ventes"),
        NAV_EXPIRATION("Voir mes expirations"),
        INFO("A propos de l'HDV"),
        RETOUR("Retour en arriére"),
        RETIRIRER("Retirer de l'hdv"),
        RECUPERER("Recuperer l'item"),
        INSPECTER("Inspecter le Joueur");
        private String displayName;


        ButtonAction(String acheter) {
            this.displayName = acheter;

        }

        public String getDisplayName() {
            return displayName;
        }
        static public ButtonAction find(String displayName){
            for(ButtonAction buttonAction : ButtonAction.values())
                if (displayName == buttonAction.displayName)
                    return buttonAction;
            return null;
        }
    }
    final static int MAX_ITEMSELL_PER_PAGE = 28;
    private List<ItemSell> itemsInHdv = new ArrayList<>();

    private List<Material> blackList = new ArrayList<>();

    public List<Material> getBlackList() {
        return blackList;
    }

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
        return itemsInHdv.stream().filter(iS -> iS.getPlayer().getPlayer().equals(seller)).collect(Collectors.toList());
    }


    public List<ItemSell> findItemExpiredOfSeller(Player seller){
        return findItemOfSeller(seller).stream().filter(iS -> iS.isExpired()).collect(Collectors.toList());
    }

    public List<ItemSell> findItemSellableOfSeller(Player seller){
        return findItemOfSeller(seller).stream().filter(iS -> !iS.isExpired()).collect(Collectors.toList());
    }





//    Génération Inventaire

    public Inventory generateInv(HDVPlayer hdvPlayer){
        System.out.println(hdvPlayer.getMenuStatus());
        if (hdvPlayer.getMenuStatus().contains("expiration")){
            return  menuExpiration(hdvPlayer);
        }else if(hdvPlayer.getMenuStatus().contains("admin")){
            return menuHotelDesDC(hdvPlayer);

        }else if(hdvPlayer.getMenuStatus().contains("mysell")){
            return menuMySell(hdvPlayer);

        } else {
            System.out.println("ouiouiouiouio");
            for(HDVPlayer hP: hdvPlug.hdvPlayers){
                if(hdvPlayer.getMenuStatus().contains(hP.getPlayer().getName()))
                    return menuInspectionPlayer(hdvPlayer);
            }
            System.out.println("nonononono");
            int p = hdvPlayer.getPage();
            if (p>=0){
                return menuHotelDesVentes(p);

            }else{

                hdvPlayer.getPlayer().sendMessage("menuStatus non trouvé");
                hdvPlayer.setMenuStatus("0");
                return menuHotelDesVentes(0);
            }

            // manque Syteme de confirmation Article a geré ici



        }

    }


    private void addMenuConfirmationPatern(Inventory inv,String leftBackBtn, String rightBtn){
        inv.setItem(9*2+2, new ItemGenerator(Material.GLASS).setName(leftBackBtn).generate());
        inv.setItem(9*2+1, new ItemGenerator(Material.GLASS).setName(leftBackBtn).generate());
        inv.setItem(9*3+2, new ItemGenerator(Material.GLASS).setName(leftBackBtn).generate());
        inv.setItem(9*3+1, new ItemGenerator(Material.GLASS).setName(leftBackBtn).generate());
        inv.setItem(9+2, new ItemGenerator(Material.GLASS).setName(leftBackBtn).generate());
        inv.setItem(9+1, new ItemGenerator(Material.GLASS).setName(leftBackBtn).generate());

        inv.setItem(9*4, new ItemGenerator(Material.BARRIER).setName(leftBackBtn).generate());


        inv.setItem(9*2+6, new ItemGenerator(Material.GLASS).setName(rightBtn).generate());
        inv.setItem(9*2+7, new ItemGenerator(Material.GLASS).setName(rightBtn).generate());
        inv.setItem(9*3+6, new ItemGenerator(Material.GLASS).setName(rightBtn).generate());
        inv.setItem(9*3+7, new ItemGenerator(Material.GLASS).setName(rightBtn).generate());
        inv.setItem(9+6, new ItemGenerator(Material.GLASS).setName(rightBtn).generate());
        inv.setItem(9+7, new ItemGenerator(Material.GLASS).setName(rightBtn).generate());

    }

    public Inventory menuConfirmationAchat(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation d'achat");
        inv.setItem(9*2+4, itemSell.getItemWithDesc());
        addMenuConfirmationPatern(inv,ButtonAction.RETOUR.displayName, ButtonAction.ACHETER.displayName);

        return inv;
    }
    public Inventory menuConfirmationAnulation(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation d'anulation de vente");
        inv.setItem(4, new ItemGenerator(Material.SKULL_ITEM).setSkullPlayer(itemSell.getPlayer().getPlayer()).setName(ButtonAction.INSPECTER.getDisplayName()).generate());
        inv.setItem(9*2+4, itemSell.getItemWithDesc());
        addMenuConfirmationPatern(inv,ButtonAction.RETOUR.displayName, ButtonAction.RETIRIRER.displayName);

        return inv;
    }
    public Inventory menuConfirmationRecuperation(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation récupération de vente");
        inv.setItem(9*2+4, itemSell.getItemWithDesc());
        addMenuConfirmationPatern(inv,ButtonAction.RETOUR.displayName, ButtonAction.RECUPERER.displayName);

        return inv;
    }
    public void addButtonNavigation(int page, int maxPage, Inventory inv){
        if (page <  maxPage)
            inv.setItem(50, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.SUIVANT.displayName).generate());
        if (page > 0)
            inv.setItem(48, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.PRECENDANT.displayName).generate());
        inv.setItem(49, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.ACTUALISER.displayName).setLore(Arrays.asList("Page "+(page+1)+" sur "+(maxPage+1))).generate());

    }

    public int verifyPage(int page, int maxPage){
        return page<0?0:page>maxPage?maxPage:page;
    }
    public Inventory menuExpiration(HDVPlayer hdvPlayer){
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Mes Expirations");
        List<ItemSell>itemExpired = findItemExpiredOfSeller(hdvPlayer.getPlayer());
        System.out.println("article : "+itemExpired.size());
        int page = 0;
        if (hdvPlayer.getMenuStatus().split("/").length==2){
            page = Integer.parseInt(hdvPlayer.getMenuStatus().split("/")[1]);
        }else hdvPlayer.setMenuStatus("expiration/0");
        verifyPage(page, itemExpired.size()/(MAX_ITEMSELL_PER_PAGE-1));
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        addButtonNavigation(page,itemExpired.size()/(MAX_ITEMSELL_PER_PAGE-1) ,inv);
        inv.setItem(7, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.NAV_HDV.displayName).generate());
        inv.setItem(8, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.NAV_VENTES.displayName).generate());
        List<String> infos = new ArrayList<>();
        infos.add("Les expirations sont des articles mis en ventes");
        infos.add("qui n'ont pas trouvé d'acheteur aprés une semaine");
        infos.add("Vous pouvez les récupérer en le selectionant");
        infos.add("Astuce : Si votre article n'as pas trouvé d'acheteur");
        infos.add("c'est que sont prix est surment trop élevé!");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());

        int itemSellPos = 10;
        for(int j = 0; j<4; j++){
            for(int i = 0; i<7; i++){
                int id = j*7+i+startId;
                if (itemExpired.size()>id && itemExpired.get(id) != null){
                    inv.setItem(itemSellPos+j*7+i, itemExpired.get(id).getItemWithDesc());
                }

            }
            itemSellPos+=2;

        }
        return inv;
    }

    public Inventory menuHotelDesVentes(int page){
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        verifyPage(page, getMaxPageHdv());
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Hotel de ventes");
        inv.setItem(7, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.NAV_VENTES.displayName).generate());
        inv.setItem(8, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.NAV_EXPIRATION.displayName).generate());
        List<String> infos = new ArrayList<>();
        infos.add("L'hotel de ventes est accessible uniquement via le courtier");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());
        List<ItemSell>itemSellable = getItemSellable();
        addButtonNavigation(page,itemSellable.size()/(MAX_ITEMSELL_PER_PAGE-1) ,inv);

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

    public Inventory menuHotelDesDC(HDVPlayer hdvPlayer){
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Hotel des Dragons Celeste");
        int page = 0;
        if (hdvPlayer.getMenuStatus().split("/").length==2){
            page = Integer.parseInt(hdvPlayer.getMenuStatus().split("/")[1]);
        }else hdvPlayer.setMenuStatus("admin/0");
        verifyPage(page, getMaxPageHdv());
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        List<String> infos = new ArrayList<>();
        infos.add("L'hotel de ventes est accessible uniquement via le courtier");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());
        List<ItemSell>itemSellable = getItemSellable();
        addButtonNavigation(page,itemSellable.size()/(MAX_ITEMSELL_PER_PAGE-1) ,inv);

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

    public Inventory menuInspectionPlayer(HDVPlayer hdvPlayer){

        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Inspection Joueur");
        int page = 0;
        String[] spliter =hdvPlayer.getMenuStatus().split("/");
        if (spliter.length==2){
            page = Integer.parseInt(spliter[1]);
        }else hdvPlayer.setMenuStatus(spliter[0]+"/0");
        HDVPlayer playerToInspect = hdvPlug.findHdvPlayer(spliter[0]);
        if (playerToInspect == null){
            hdvPlayer.getPlayer().sendMessage("Joueur non trouvé");
            hdvPlayer.setMenuStatus("0");
            return generateInv(hdvPlayer);

        }
        verifyPage(page, getMaxPageHdv());
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        List<String> infos = new  ArrayList<>();
        infos.add("L'hotel de ventes est accessible uniquement via le courtier");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());
        List<ItemSell>itemSells = findItemOfSeller(playerToInspect.getPlayer());
        addButtonNavigation(page,itemSells.size()/(MAX_ITEMSELL_PER_PAGE-1) ,inv);

        int itemSellPos = 10;
        for(int j = 0; j<4; j++){
            for(int i = 0; i<7; i++){
                int id = j*7+i+startId;
                if (itemSells.size()>id && itemSells.get(id) != null){
                    inv.setItem(itemSellPos+j*7+i, itemSells.get(id).getItemWithDesc());
                }

            }
            itemSellPos+=2;

        }
        return inv;

    }
    private Inventory menuMySell(HDVPlayer hdvPlayer) {
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Mes ventes");
        int page = 0;
        String[] spliter =hdvPlayer.getMenuStatus().split("/");
        if (spliter.length==2){
            page = Integer.parseInt(spliter[1]);
        }else hdvPlayer.setMenuStatus("mysell/"+0);
        verifyPage(page, getMaxPageHdv());
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        List<String> infos = new  ArrayList<>();
        infos.add("L'hotel de ventes est accessible uniquement via le courtier");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());
        inv.setItem(7, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.NAV_EXPIRATION.displayName).generate());
        inv.setItem(8, new ItemGenerator(new ItemStack(Material.STAINED_GLASS, 1)).setName(ButtonAction.NAV_HDV.displayName).generate());
        List<ItemSell>itemSells = findItemSellableOfSeller(hdvPlayer.getPlayer());
        addButtonNavigation(page,itemSells.size()/(MAX_ITEMSELL_PER_PAGE-1) ,inv);

        int itemSellPos = 10;
        for(int j = 0; j<4; j++){
            for(int i = 0; i<7; i++){
                int id = j*7+i+startId;
                if (itemSells.size()>id && itemSells.get(id) != null){
                    inv.setItem(itemSellPos+j*7+i, itemSells.get(id).getItemWithDesc());
                }

            }
            itemSellPos+=2;

        }
        return inv;
    }

    public int getMaxPageHdv() {
        return getItemSellable().size()/MAX_ITEMSELL_PER_PAGE-1;
    }
}
