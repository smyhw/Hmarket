package cat.nyaa.hmarket.gui;

import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.database.StoreModel;
import cat.nyaa.hmarket.database.StoreTable;
import cat.nyaa.hmarket.gui.item.UiShopItem;
import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaacore.utils.OfflinePlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SellSignShopUI extends ShopUI {

    private final UUID owner;

    public SellSignShopUI(UUID owner, List<ItemCache> goods) {
        super(goods);
        this.owner = owner;
    }

    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (check(slotNum, clickType, player)) return;

        var target = goods.get(slotNum);
        // TODO: Eco check
        var item = ((UiShopItem) uiItemList.get(slotNum)).nbt().clone();
        item.setAmount(1);
        if (!InventoryUtils.hasEnoughSpace(player, item, 1)) {
            return;
        }

        InventoryUtils.addItem(player.getInventory(), item);
        var ownerPlayer = Objects.requireNonNullElse(Bukkit.getPlayer(this.owner), Bukkit.getOfflinePlayer(this.owner));


        new Message("").append(I18n.format("shop.transaction.sell", ownerPlayer.getName()), target.nbt).send(player);
    }


}
