package cat.nyaa.hmarket.shopitem;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemCache {
    public UUID uuid;
    public ItemStack nbt;
    public double price;
    public ShopItemType type;
    public UUID owner;
    public boolean isDirty = false;

    public ItemCache(UUID uuid, ItemStack nbt, double price, ShopItemType type) {
        this.uuid = uuid;
        this.nbt = nbt;
        this.price = price;
        this.type = type;
    }
}
