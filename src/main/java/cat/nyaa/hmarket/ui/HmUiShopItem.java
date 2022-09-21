package cat.nyaa.hmarket.ui;

import cat.nyaa.aolib.aoui.data.WindowClickData;
import cat.nyaa.aolib.aoui.item.IClickableUiItem;
import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.db.data.ShopItemData;
import cat.nyaa.hmarket.ui.data.MarketUiItemNamespacedKey;
import cat.nyaa.hmarket.utils.HMMathUtils;
import cat.nyaa.hmarket.utils.TimeUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HmUiShopItem implements IClickableUiItem {
    private final ShopItemData itemData;
    private final long itemTime;


    public HmUiShopItem(@NotNull ShopItemData shopItemData) {
        this.itemData = shopItemData;
        this.itemTime = TimeUtils.getUnixTimeStampNow();
    }

    @Override
    public void onClick(@NotNull WindowClickData clickData, Player player) {
        var clickType = clickData.clickType();
        var buttonNum = clickData.buttonNum();
        var targetItem = getTargetItem(clickData);
        if (targetItem == null) return;
        if (!targetItem.getType().isAir() && !checkCarriedItem(targetItem)) return;
        if (clickType == DataClickType.PICKUP && buttonNum == 0) {
            onBuy(player, 1);
            return;
        }
        if (clickType == DataClickType.QUICK_MOVE && buttonNum == 0) {
            onBuy(player, itemData.amount());
        }

    }

    private @Nullable ItemStack getTargetItem(@NotNull WindowClickData clickData) {
        var carriedItem = clickData.carriedItem();
        if (carriedItem != null && !carriedItem.getType().isAir()) return clickData.carriedItem();
        if (clickData.changedSlots().size() == 1) return clickData.changedSlots().get(0);
        if (clickData.changedSlots().size() > 1) {
            for (int i : clickData.changedSlots().keySet()) {
                if (!clickData.changedSlots().get(i).getType().isAir()) return clickData.changedSlots().get(i);
            }
        }
        return null;
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

    @Override
    public ItemStack getWindowItem(Player player) {
        var api = Hmarket.getAPI();
        if (api == null) return new ItemStack(Material.AIR);
        var item = ItemStackUtils.itemFromBase64(itemData.itemNbt());
        item.setAmount(itemData.amount());
        var ownerName = Bukkit.getOfflinePlayer(itemData.owner()).getName();
        var meta = item.getItemMeta();
        if (meta != null) {
            var lore = meta.lore();
            if (lore == null) lore = Lists.newArrayList();
            lore.add(Component.text(HMI18n.format("info.ui.item.owner", ownerName == null ? itemData.owner() : ownerName)));
            lore.add(Component.text(HMI18n.format("info.ui.item.price", itemData.price())));
            lore.add(Component.text(HMI18n.format("info.ui.item.tax", api.getMarketAPI().getTaxRate(itemData) * 100.0, HMMathUtils.round((itemData.price() * (1.0 + api.getMarketAPI().getTaxRate(itemData))), 2))));
            if (itemData.owner().equals(player.getUniqueId())) {
                lore.add(Component.text(HMI18n.format("info.ui.item.owner_item_back")));
            } else {
                lore.add(Component.text(HMI18n.format("info.ui.item.buy_item")));
            }
            meta.lore(lore);
            MarketUiItemNamespacedKey.getInstance().ifPresent(namespacedKey -> {
                meta.getPersistentDataContainer().set(namespacedKey.itemId(), PersistentDataType.INTEGER, itemData.itemId());
                meta.getPersistentDataContainer().set(namespacedKey.MarketId(), PersistentDataType.STRING, itemData.market().toString());
                meta.getPersistentDataContainer().set(namespacedKey.ItemTime(), PersistentDataType.LONG, itemTime);
            });
            item.setItemMeta(meta);
        }
        return item;
    }
}
