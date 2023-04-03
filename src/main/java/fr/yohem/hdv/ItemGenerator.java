package fr.yohem.hdv;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemGenerator{
    ItemStack item;
    ItemMeta meta;

    public ItemGenerator(ItemStack item, ItemMeta meta) {
        this.item = item;
        this.meta = meta;
    }

    public ItemGenerator(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }
    public ItemGenerator(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }
    public ItemStack generate(){
        item.setItemMeta(meta);
        return item;
    }
    public ItemGenerator setName(String name){
        meta.setDisplayName(name);
        return this;
    }
    public ItemGenerator setLore(List<String> lore){
        meta.setLore(lore);
        return this;
    }
    public ItemGenerator setEnchant(Enchantment enchant, int level){
        meta.addEnchant(enchant,level, true);
        return this;
    }
    public ItemGenerator setEnchant(boolean isUnbreakable){
        meta.setUnbreakable(isUnbreakable);
        return this;
    }


}