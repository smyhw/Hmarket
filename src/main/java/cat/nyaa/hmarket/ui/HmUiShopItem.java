package cat.nyaa.hmarket.ui;

import cat.nyaa.aolib.aoui.item.IClickableUiItem;
import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.data.ShopItemData;
import cat.nyaa.hmarket.utils.HMMathUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class HmUiShopItem implements IClickableUiItem {
    private final Runnable updateCallback;
    private final ShopItemData itemData;
    private final Consumer<Boolean> setLocked;

    public HmUiShopItem(ShopItemData shopItemData, Runnable UpdateCallback, Consumer<Boolean> setLocked) {
        this.itemData = shopItemData;
        this.updateCallback = UpdateCallback;
        this.setLocked = setLocked;

    }

    @Override
    public void onClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (clickType == DataClickType.PICKUP && buttonNum == 0) {
            onBuy(player, 1);
            return;
        }
        if (clickType == DataClickType.QUICK_MOVE && buttonNum == 0) {
            onBuy(player, itemData.amount());
            return;
        }


    }

    private void onBuy(Player player, int amount) {
        var hMarketAPI = Hmarket.getAPI();
        if (hMarketAPI == null) return;
        try {
            setLocked.accept(true);
            hMarketAPI.buy(player, itemData, amount).thenApplyAsync((b) -> {
                if (b) {
                    HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.buy_success");
                } else {
                    HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.buy_failed");
                }
                TaskUtils.async.callSyncAndGet(() -> {
                    setLocked.accept(false);
                    updateCallback.run();
                    return null;
                });
                return b;
            });
        } catch (NotEnoughMoneyException e) {
            HMI18n.send(player, "info.ui.market.not_enough_money");
        } catch (NotEnoughItemsException e) {
            HMI18n.send(player, "info.ui.market.out_of_stock");
            updateCallback.run();
        }
    }

    @Override
    public void onClick(DataClickType clickType, Player player) {

    }

    @Override
    public ItemStack getWindowItem(Player player) {
        var api = Hmarket.getAPI();
        if (api == null) return new ItemStack(Material.AIR);
        var item = ItemStackUtils.itemFromBase64(itemData.itemNbt());
        item.setAmount(itemData.amount());
        var ownerName = Bukkit.getOfflinePlayer(UUID.fromString(itemData.owner())).getName();
        var meta = item.getItemMeta();
        if (meta != null) {
            var lore = meta.getLore();
            if (lore == null) lore = Lists.newArrayList();
            lore.add(HMI18n.format("info.ui.item.owner", ownerName == null ? itemData.owner() : ownerName));
            lore.add(HMI18n.format("info.ui.item.price", itemData.price()));
            lore.add(HMI18n.format("info.ui.item.tax", api.getFeeRate(itemData) * 100.0, HMMathUtils.round((itemData.price() * (1.0 + api.getFeeRate(itemData))), 2)));
            if (UUID.fromString(itemData.owner()).equals(player.getUniqueId())) {
                lore.add(HMI18n.format("info.ui.item.owner_item_back"));
            } else {
                lore.add(HMI18n.format("info.ui.item.buy_item"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
