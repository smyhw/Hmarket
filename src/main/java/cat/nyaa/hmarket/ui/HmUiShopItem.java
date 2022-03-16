package cat.nyaa.hmarket.ui;

import cat.nyaa.aolib.aoui.item.IClickableUiItem;
import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.data.ShopItemData;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class HmUiShopItem implements IClickableUiItem {
    private final Runnable updateCallback;
    private final ShopItemData itemData;

    public HmUiShopItem(ShopItemData shopItemData, Runnable UpdateCallback) {
        this.itemData = shopItemData;
        this.updateCallback = UpdateCallback;
    }

    @Override
    public void onClick(DataClickType clickType, Player player) {
        var hMarketAPI = Hmarket.getAPI();
        if (hMarketAPI == null) return;
        try {
            hMarketAPI.buy(player, itemData, 1).thenApplyAsync((b) -> {
                if (b) {
                    HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.buy_success");
                } else {
                    HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.buy_failed");
                }
                updateCallback.run();
                return b;
            });
        } catch (NotEnoughMoneyException e) {
            HMI18n.send(player, "info.ui.market.not_enough_money");
        } catch (NotEnoughItemsException e) {
            HMI18n.send(player, "info.ui.market.out_of_stock");
        }
    }

    @Override
    public ItemStack getWindowItem(Player player) {
        var item = ItemStackUtils.itemFromBase64(itemData.itemNbt());
        item.setAmount(itemData.amount());
        var ownerName = Bukkit.getOfflinePlayer(UUID.fromString(itemData.owner())).getName();
        var meta = item.getItemMeta();
        if (meta != null) {
            var lore = meta.getLore();
            if (lore == null) lore = Lists.newArrayList();
            lore.add(HMI18n.format("info.ui.item.owner", ownerName == null ? itemData.owner() : ownerName));
            lore.add(HMI18n.format("info.ui.item.price", itemData.price()));
            lore.add(HMI18n.format("info.ui.item.owner_item_back"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
