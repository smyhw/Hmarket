package cat.nyaa.hmarket.shopitem;

import org.bukkit.inventory.ItemStack;

public class ItemCache {
    public ItemStack nbt;
    public double price;
    public ShopItemType type;

    public ItemCache(ItemStack nbt, double price, ShopItemType type) {
        this.nbt = nbt;
        this.price = price;
        this.type = type;
    }
}
