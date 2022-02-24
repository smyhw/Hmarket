package cat.nyaa.hmarket.shopitem;

import cat.nyaa.hmarket.database.models.ItemsModel;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopItem {
    public UUID uuid;
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

    public void randomUUID() {
        if (uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    public ItemsModel toItemModel() {
        return new ItemsModel(ItemStackUtils.itemToBase64(nbt), price, owner, type.name(), uuid);
    }

    public ItemCache toCache() {
        return new ItemCache(this.uuid, this.nbt, this.price, this.type);
    }

    public UUID getOwner() {
        return owner;
    }

}
