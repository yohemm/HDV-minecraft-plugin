package fr.yohem.hdv;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MenuManager {
    public enum ButtonAction {
        ACHETER("Acheter"),
        SUIVANT("--> Page Suivante"),
        PRECENDANT("Page Precendante <--"),
        ACTUALISER("Actualiser la page"),
        NAV_HDV("Retour à l'§ehôtel des ventes§r"),
        NAV_VENTES("Voir mes §aventes§r"),
        NAV_EXPIRATION("Voir mes §cexpirations§r"),
        INFO("À propos de l'HDV"),
        RETOUR("Retour en arrière"),
        RETIRIRER("Retirer de l'hdv"),
        RECUPERER("Récupérer l'item"),
        INSPECTER("Inspecter le joueur");
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

    final static private ItemStack bgGlass = new ItemGenerator(Material.STAINED_GLASS_PANE).setName("§8[§6MineHDV§8]§r").generate();
    final static private ItemStack gGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1, (byte) 5);
    final static private ItemStack rGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1, (byte) 14);


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


    public void update(List<ItemSell> importItems) {
        if (importItems!=null)
            for (ItemSell it : importItems)
                if (it != null && !itemsInHdv.contains(it)) {
                    itemsInHdv.add(it);
                    if ( hdvPlug.hdvPlayers.stream().filter(h -> h.getPlayer().getUniqueId().equals(it.getPlayer())).collect(Collectors.toList()).isEmpty()){
                        hdvPlug.hdvPlayers.add(new HDVPlayer(Bukkit.getOfflinePlayer(it.getPlayer())));
                    }
                }

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
        blackList.add(Material.AIR);
    }

    public List<ItemSell> getItemSellable(){
        List<ItemSell> itemSells = itemsInHdv.stream().filter(iS -> new Date().getTime() - iS.getDate() < 604800000).collect(Collectors.toList());
        Collections.reverse(itemSells);
        return itemSells;
    }

    public List<ItemSell> findItemOfSeller(OfflinePlayer seller){
        return itemsInHdv.stream().filter(iS -> iS.getPlayer().equals(seller.getUniqueId())).collect(Collectors.toList());
    }


    public List<ItemSell> findItemExpiredOfSeller(OfflinePlayer seller){
        return findItemOfSeller(seller).stream().filter(iS -> iS.isExpired()).collect(Collectors.toList());
    }

    public List<ItemSell> findItemSellableOfSeller(OfflinePlayer seller){
        return findItemOfSeller(seller).stream().filter(iS -> !iS.isExpired()).collect(Collectors.toList());
    }





//    Génération Inventaire

    public Inventory generateInv(HDVPlayer hdvPlayer){
        if (hdvPlayer.getMenuStatus().contains("expiration")){
            return  menuExpiration(hdvPlayer);
        }else if(hdvPlayer.getMenuStatus().contains("admin")){
            return menuHotelDesDC(hdvPlayer);

        }else if(hdvPlayer.getMenuStatus().contains("mysell")){
            return menuMySell(hdvPlayer);

        } else {
            for(HDVPlayer hP: hdvPlug.hdvPlayers){
                if(hdvPlayer.getMenuStatus().contains(hP.getPlayer().getName()))
                    return menuInspectionPlayer(hdvPlayer);
            }
            int p = hdvPlayer.getPage();
            if (p>=0){
                return menuHotelDesVentes(p);

            }else{

                ((Player)hdvPlayer.getPlayer()).sendMessage("menuStatus non trouvé");
                hdvPlayer.setMenuStatus("0");
                return menuHotelDesVentes(0);
            }

            // manque Syteme de confirmation Article a geré ici



        }

    }


    private void addMenuConfirmationPatern(Inventory inv,String leftBackBtn, String rightBtn){
        for (int i = 0; i<45; i++) {
            if (Arrays.asList(9 * 2 + 2,9 * 2 + 1,9 * 3 + 2,9 * 3 + 1,9 + 2,9 + 1).contains(i))
                inv.setItem(i, new ItemGenerator(rGlass).setName(leftBackBtn).generate());
            else if (Arrays.asList(9 * 2 + 7,9 * 2 + 6,9 * 3 + 7,9 * 3 + 6,9 + 7,9 + 6).contains(i))
                inv.setItem(i, new ItemGenerator(gGlass).setName(rightBtn).generate());
            else
                inv.setItem(i, new ItemGenerator(bgGlass).setName("").generate());
        }
        inv.setItem(9 * 4, new ItemGenerator(Material.BARRIER).setName(leftBackBtn).generate());
    }

    public Inventory menuConfirmationAchat(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation d'achat");
        addMenuConfirmationPatern(inv,ButtonAction.RETOUR.displayName, ButtonAction.ACHETER.displayName);
        inv.setItem(9*2+4, itemSell.getItemWithDesc());

        return inv;
    }
    public Inventory menuConfirmationAnulation(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation d'annulation de vente");
        addMenuConfirmationPatern(inv,ButtonAction.RETOUR.displayName, ButtonAction.RETIRIRER.displayName);
        OfflinePlayer playerOfItem =Bukkit.getOfflinePlayer(itemSell.getPlayer());
        inv.setItem(4, new ItemGenerator(new ItemStack(Material.SKULL_ITEM, 1, (short) 3)).setName(ButtonAction.INSPECTER.getDisplayName()).setLore(Arrays.asList("§8Joueur : §6§l"+playerOfItem.getName(),"§8Compte : §2"+HDV.getEconomy().getBalance(playerOfItem)+"§8$")).setSkullPlayer(playerOfItem).generate());
        inv.setItem(9*2+4, itemSell.getItemWithDesc());

        return inv;
    }
    public Inventory menuConfirmationRecuperation(ItemSell itemSell){
        Inventory inv = hdvPlug.getServer().createInventory(null,45, "§8 Confirmation récupération de vente");
        addMenuConfirmationPatern(inv,ButtonAction.RETOUR.displayName, ButtonAction.RECUPERER.displayName);
        inv.setItem(9*2+4, itemSell.getItemWithDesc());

        return inv;
    }
    public void addButtonNavigation(int page, double maxPage, Inventory inv){
        for (int i=0; i < 9; i++){
            inv.setItem(i, bgGlass);
            inv.setItem(inv.getSize()-(i+1), bgGlass);
        }
        inv.setItem(9, bgGlass);
        inv.setItem(17, bgGlass);
        inv.setItem(18, bgGlass);
        inv.setItem(26, bgGlass);
        inv.setItem(27, bgGlass);
        inv.setItem(35, bgGlass);
        inv.setItem(36, bgGlass);
        inv.setItem(44, bgGlass);

        if (page <  maxPage) {
            inv.setItem(50, new ItemGenerator(new ItemStack(Material.PAPER, 1)).setName(ButtonAction.SUIVANT.displayName).generate());
        }
        if (page > 0)
            inv.setItem(48, new ItemGenerator(new ItemStack(Material.PAPER, 1)).setName(ButtonAction.PRECENDANT.displayName).generate());
        inv.setItem(49, new ItemGenerator(new ItemStack(Material.DOUBLE_PLANT, 1, (short) 1, (byte) 0)).setName(ButtonAction.ACTUALISER.displayName).setLore(Arrays.asList("Page "+(page+1)+" sur "+((int)maxPage+2))).generate());

    }

    public int verifyPage(int page, int maxPage){
        return page<0?0:page>maxPage?maxPage:page;
    }
    public Inventory menuExpiration(HDVPlayer hdvPlayer){
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Mes Expirations");
        List<ItemSell>itemExpired = findItemExpiredOfSeller(hdvPlayer.getPlayer());
        int page = 0;
        if (hdvPlayer.getMenuStatus().split("/").length==2){
            page = Integer.parseInt(hdvPlayer.getMenuStatus().split("/")[1]);
        }else hdvPlayer.setMenuStatus("expiration/0");
        verifyPage(page, (int) ((double)itemExpired.size()/(double)MAX_ITEMSELL_PER_PAGE - 1));
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        addButtonNavigation(page,((double)itemExpired.size()/(double)MAX_ITEMSELL_PER_PAGE - 1) ,inv);
        inv.setItem(7, new ItemGenerator(new ItemStack(Material.BED, 1)).setName(ButtonAction.NAV_HDV.displayName).generate());
        inv.setItem(8, new ItemGenerator(gGlass).setName(ButtonAction.NAV_VENTES.displayName).generate());
        List<String> infos = new ArrayList<>();
        infos.add("§7Les expirations sont des articles mis en vente");
        infos.add("§7qui n'ont pas trouvé d'acheteur après une semaine.");
        infos.add("§7Vous pouvez les récupérer en le sélectionnant !");
        infos.add("§6Astuce§7 : Si votre article n'a pas trouvé d'acheteur");
        infos.add("§7c'est que son prix est sûrement trop élevé!§r");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.SIGN, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());

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
        verifyPage(page, (int)getMaxPageHdv());
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Courtier");
        List<ItemSell>itemSellable = getItemSellable();
        addButtonNavigation(page,getMaxPageHdv() ,inv);
        inv.setItem(7, new ItemGenerator(gGlass).setName(ButtonAction.NAV_VENTES.displayName).generate());
        inv.setItem(8, new ItemGenerator(rGlass).setName(ButtonAction.NAV_EXPIRATION.displayName).generate());
        List<String> infos = new ArrayList<>();
        infos.add("§7§7L'hôtel des ventes permet de rendre accessibles§r");
        infos.add("§7des items à la vente et à l'achat pour tous les joueurs.§r");
        infos.add("§6Asctuce :§7 /hdv sell <prince>, pour vendre un item dans vos mains§r");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.SIGN, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());

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
        Inventory inv = hdvPlug.getServer().createInventory(null, 54, "§8 Hôtel des Dragons Céleste");
        int page = 0;
        if (hdvPlayer.getMenuStatus().split("/").length==2){
            page = Integer.parseInt(hdvPlayer.getMenuStatus().split("/")[1]);
        }else hdvPlayer.setMenuStatus("admin/0");
        verifyPage(page, (int) getMaxPageHdv());
        List<ItemSell>itemSellable = getItemSellable();
        addButtonNavigation(page,getMaxPageHdv() ,inv);
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        List<String> infos = new ArrayList<>();
        infos.add("§7L'hôtel des Dragons Céleste permet de réguler§r");
        infos.add("§7les items mis en vente dans l'hdv§r");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.SIGN, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());

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
            ((Player)hdvPlayer.getPlayer()).sendMessage("Joueur non trouvé");
            hdvPlayer.setMenuStatus("0");
            return generateInv(hdvPlayer);

        }
        List<ItemSell>itemSells = findItemOfSeller(playerToInspect.getPlayer());
        addButtonNavigation(page,(double)itemSells.size()/(double)MAX_ITEMSELL_PER_PAGE -1 ,inv);
        verifyPage(page, (int)((double)itemSells.size()/(double)MAX_ITEMSELL_PER_PAGE -1));
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        List<String> infos = new  ArrayList<>();
        infos.add("§7Les inspections des joueurs permettent de voir");
        infos.add("§7les items en §lventes§r§7 et §lexpirer§r§7 du joueur§r");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.SIGN, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());

        OfflinePlayer playerOfItem = playerToInspect.getPlayer();
        inv.setItem(4, new ItemGenerator(new ItemStack(Material.SKULL_ITEM, 1, (short) 3)).setName(ButtonAction.INSPECTER.getDisplayName()).setLore(Arrays.asList("§8Joueur : §6§l"+playerOfItem.getName(),"§8Compte : §2"+HDV.getEconomy().getBalance(playerOfItem)+"§8$")).setSkullPlayer(playerOfItem).generate());
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
        verifyPage(page, (int) getMaxPageHdv());
        List<ItemSell>itemSells = findItemSellableOfSeller(hdvPlayer.getPlayer());
        addButtonNavigation(page,(double)itemSells.size()/(double)MAX_ITEMSELL_PER_PAGE-1 ,inv);
        int startId = MAX_ITEMSELL_PER_PAGE*page;
        List<String> infos = new  ArrayList<>();
        infos.add("§7Les ventes sont les items encore visibles");
        infos.add("§7l'hôtel des ventes pour tout le monde");
        infos.add("§7qui n'ont pas trouvé d'acheteur dans l'HDV.");
        infos.add("§6Asctuce : §7Vous pouvez les récupérer en le sélectionnant§r");
        inv.setItem(0, new ItemGenerator(new ItemStack(Material.SIGN, 1)).setName(ButtonAction.INFO.displayName).setLore(infos).generate());
        inv.setItem(7, new ItemGenerator(rGlass).setName(ButtonAction.NAV_EXPIRATION.displayName).generate());
        inv.setItem(8, new ItemGenerator(new ItemStack(Material.BED, 1)).setName(ButtonAction.NAV_HDV.displayName).generate());

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

    public double getMaxPageHdv() {
        return ((double)getItemSellable().size()/(double)MAX_ITEMSELL_PER_PAGE) -1;
    }

    public List<String> getMaterialBlack(){
        return blackList.stream().filter(m->!Material.AIR.equals(m)).map(Enum::name).collect(Collectors.toList());
    }
    public void setMaterialBlack(List<String> newBlacks){
        blackList.addAll(newBlacks.stream().distinct().map(Material::matchMaterial).collect(Collectors.toList()));
    }

    public void exportBlackList(HDV hdv){
        final File file = new File(hdv.getDataFolder()+"/blackList.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("blackList",getMaterialBlack());
        try {
            configuration.save(file);
        }catch (IOException e) {
            System.out.println("BLACKLIST NOT EXPORT");
            throw new RuntimeException(e);
        }
    }
    public void importBlackList(HDV hdv){
        final File file = new File(hdv.getDataFolder()+"/blackList.yml");
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        if (configuration.contains("blackList"))
            setMaterialBlack((List<String>) configuration.get("blackList"));


    }
}
