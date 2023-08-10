package cat.nyaa.hmarket.ui.data;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.db.data.ShopItemData;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ShopItemDataUtils {
    private ItemMeta meta;

    public static ItemStack getWindowedItem(Player player, ShopItemData itemData) {
        var api = Hmarket.getAPI();
        if (api == null) return new ItemStack(Material.AIR);
        var item = ItemStackUtils.itemFromBase64(itemData.itemNbt());
        item.setAmount(itemData.amount());
        var ownerName = Bukkit.getOfflinePlayer(itemData.owner()).getName();
        var meta = item.getItemMeta();
        if (meta != null) {
            var lore = meta.lore();
            if (lore == null) lore = Lists.newArrayList();
            lore.add(HMI18n.format("info.ui.item.owner", ownerName == null ? itemData.owner() : ownerName));
            lore.add(HMI18n.format("info.ui.item.price", itemData.price()));
            lore.add(HMI18n.format("info.ui.item.tax", itemData.price() * (1.0 + api.getMarketAPI().getTaxRate(itemData)), api.getMarketAPI().getTaxRate(itemData) * 100.0));
            if (itemData.owner().equals(player.getUniqueId())) {
                lore.add(HMI18n.format("info.ui.item.owner_item_back"));

            } else {
                lore.add(HMI18n.format("info.ui.item.buy_item"));
            }
            meta.lore(lore);
            MarketUiItemNamespacedKey.getInstance().ifPresent(namespacedKey -> {
                meta.getPersistentDataContainer().set(namespacedKey.itemId(), PersistentDataType.INTEGER, itemData.itemId());
                meta.getPersistentDataContainer().set(namespacedKey.MarketId(), PersistentDataType.STRING, itemData.market().toString());
                meta.getPersistentDataContainer().set(namespacedKey.ItemTime(), PersistentDataType.LONG, itemData.createdAt());
                meta.getPersistentDataContainer().set(namespacedKey.ItemOwnerUniqueID(), PersistentDataType.STRING, itemData.owner().toString());
            });
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean checkIfIsWindowedItem(ItemStack itemStack) {
        var namespacedKey = MarketUiItemNamespacedKey.getInstance().get();
        var meta = itemStack.getItemMeta();
        if (meta != null) {
            var container = meta.getPersistentDataContainer();
            return container.has(namespacedKey.MarketId(), PersistentDataType.STRING);
        }
        return false;
    }

    public static UUID getMarketIDFromItemStack(ItemStack itemStack) {
        //check if is windowed item
        if (!checkIfIsWindowedItem(itemStack)) return null;
        var namespacedKey = MarketUiItemNamespacedKey.getInstance().get();
        var meta = itemStack.getItemMeta();
        if (meta != null) {
            var container = meta.getPersistentDataContainer();
            if (container.has(namespacedKey.MarketId(), PersistentDataType.STRING)) {
                return UUID.fromString(container.get(namespacedKey.MarketId(), PersistentDataType.STRING));
            }
        }
        return null;
    }

    public static int getMarketItemIDFromItemStack(ItemStack itemStack) {
        //check if is windowed item
        if (!checkIfIsWindowedItem(itemStack)) return -1;
        var namespacedKey = MarketUiItemNamespacedKey.getInstance().get();
        var meta = itemStack.getItemMeta();
        if (meta != null) {
            var container = meta.getPersistentDataContainer();
            if (container.has(namespacedKey.itemId(), PersistentDataType.INTEGER)) {
                return container.get(namespacedKey.itemId(), PersistentDataType.INTEGER);
            }
        }
        return -1;
    }

    public static long getMarketItemUpdateTime(ItemStack itemStack) {
        //check if is windowed item
        if (!checkIfIsWindowedItem(itemStack)) return -1;
        var namespacedKey = MarketUiItemNamespacedKey.getInstance().get();
        var meta = itemStack.getItemMeta();
        if (meta != null) {
            var container = meta.getPersistentDataContainer();
            if (container.has(namespacedKey.ItemTime(), PersistentDataType.LONG)) {
                return container.get(namespacedKey.ItemTime(), PersistentDataType.LONG);
            }
        }
        return -1;
    }

    public static UUID getWindowedItemOwnerUUID(ItemStack itemStack) {
        //check if is windowed item
        if (!checkIfIsWindowedItem(itemStack)) return null;
        var namespacedKey = MarketUiItemNamespacedKey.getInstance().get();
        var meta = itemStack.getItemMeta();
        return UUID.fromString(meta.getPersistentDataContainer().get(namespacedKey.ItemOwnerUniqueID(), PersistentDataType.STRING));
    }
}
