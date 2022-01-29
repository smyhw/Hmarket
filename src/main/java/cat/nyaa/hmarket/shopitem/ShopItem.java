package cat.nyaa.hmarket.shopitem;

import cat.nyaa.aolib.aoui.item.IUiItem;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cat.nyaa.hmarket.database.ItemsModel;

import java.util.UUID;

public class ShopItem {
    private UUID owner;
    private double price;
    private ItemStack nbt;
    private ShopItemType type;

    public ShopItem(UUID owner, double price, ItemStack nbt, ShopItemType type) {
        this.owner = owner;
        this.price = price;
        this.nbt = nbt;
        this.type = type;
    }

    public ItemsModel toItemModel() {
        return new ItemsModel(ItemStackUtils.itemToBase64(nbt), price, owner, type.name());
    }

    public ItemCache toCache() {
        return new ItemCache(this.nbt, this.price, this.type);
    }

    public UUID getOwner() {
        return owner;
    }

}
