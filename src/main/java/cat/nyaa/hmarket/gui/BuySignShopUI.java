package cat.nyaa.hmarket.gui;

import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.database.StoreModel;
import cat.nyaa.hmarket.database.StoreTable;
import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BuySignShopUI extends ShopUI {

    private final UUID owner;

    public BuySignShopUI(UUID owner, List<ItemCache> goods) {
        super(goods);
        this.owner = owner;
    }

    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (check(slotNum, clickType, player)) return;
        var target = goods.get(slotNum);
        if (!InventoryUtils.hasItem(player, target.nbt, 1)) {
            new Message(I18n.format("shop.noEnoughItem")).append(target.nbt).send(player);

            return;
        }
        // TODO: Eco check

        InventoryUtils.removeItem(player, target.nbt, 1);
        new Message("").append(I18n.format("shop.transaction.buy"), target.nbt).send(player);
        try {
            StoreTable.updateItem(new StoreModel(ItemStackUtils.itemToBase64(target.nbt), 1, owner.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
