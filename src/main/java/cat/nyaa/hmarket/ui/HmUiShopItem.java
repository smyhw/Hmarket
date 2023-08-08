package cat.nyaa.hmarket.ui;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.db.data.ShopItemData;
import cat.nyaa.hmarket.ui.data.MarketUiItemNamespacedKey;
import cat.nyaa.hmarket.utils.TimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class HmUiShopItem {
    private final ShopItemData itemData;
    private final long itemTime;


    public HmUiShopItem(@NotNull ShopItemData shopItemData) {
        this.itemData = shopItemData;
        this.itemTime = TimeUtils.getUnixTimeStampNow();
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        var action = event.getAction();
        var slot = event.getSlot();
        var item = event.getCurrentItem();
        if (item == null || item.getType().isAir() ||
                !checkCarriedItem(item)) {
            event.setCancelled(true);
            return;
        }
        if (action != InventoryAction.PICKUP_ALL) {
            onBuy((Player) event.getWhoClicked(), 1);
        }
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            onBuy((Player) event.getWhoClicked(), itemData.amount());
        }
    }

    private boolean checkCarriedItem(ItemStack carriedItem) {
        var namespacedKey = MarketUiItemNamespacedKey.getInstanceNullable();
        if (namespacedKey == null) return false;
        if (!carriedItem.hasItemMeta()) return false;
        var meta = carriedItem.getItemMeta();
        if (meta == null) return false;
        var carriedItemId = meta.getPersistentDataContainer().get(namespacedKey.itemId(), PersistentDataType.INTEGER);
        var carriedItemMarketId = meta.getPersistentDataContainer().get(namespacedKey.MarketId(), PersistentDataType.STRING);
        var carriedItemTime = meta.getPersistentDataContainer().get(namespacedKey.ItemTime(), PersistentDataType.LONG);
        return carriedItemId != null && carriedItemMarketId != null && carriedItemTime != null && carriedItemId.equals(itemData.itemId()) && carriedItemMarketId.equals(itemData.market().toString()) && carriedItemTime == itemTime;
    }

    private void onBuy(Player player, int amount) {
        var hMarketAPI = Hmarket.getAPI();
        if (hMarketAPI == null) return;
        hMarketAPI.getMarketAPI().commandBuy(player, itemData.market(), itemData.itemId(), amount);
    }

}
