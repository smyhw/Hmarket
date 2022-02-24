package cat.nyaa.hmarket.database.models;

import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.hmarket.shopitem.ShopItemType;
import cat.nyaa.nyaacore.utils.ItemStackUtils;

import java.util.UUID;

public class ItemsModel {
    public UUID uuid;
    public String nbt;
    public double price;
    public UUID owner;
    public String type;

    public ItemsModel(String nbt, double price, UUID owner, String type, UUID uuid) {
        this.nbt = nbt;
        this.price = price;
        this.owner = owner;
        this.type = type;
        this.uuid = uuid;
    }

    public ItemCache toCache() {
        return new ItemCache(uuid, ItemStackUtils.itemFromBase64(nbt), price, ShopItemType.valueOf(type));
    }

    public ItemCache toCacheWithOwner() {
        var item = new ItemCache(uuid, ItemStackUtils.itemFromBase64(nbt), price, ShopItemType.valueOf(type));
        item.owner = this.owner;
        return item;
    }
}
