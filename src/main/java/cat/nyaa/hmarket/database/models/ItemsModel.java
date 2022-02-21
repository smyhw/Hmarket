package cat.nyaa.hmarket.database.models;

import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.hmarket.shopitem.ShopItemType;
import cat.nyaa.nyaacore.utils.ItemStackUtils;

import java.util.UUID;

public class ItemsModel {
    public String nbt;
    public double price;
    public UUID owner;
    public String type;

    public ItemsModel(String nbt, double price, UUID owner, String type) {
        this.nbt = nbt;
        this.price = price;
        this.owner = owner;
        this.type = type;
    }

    public ItemCache toCache() {
        return new ItemCache(ItemStackUtils.itemFromBase64(nbt), price, ShopItemType.valueOf(type));
    }
}
