package cat.nyaa.hmarket.gui;

import cat.nyaa.aolib.aoui.item.EmptyUIItem;
import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.database.StoreModel;
import cat.nyaa.hmarket.database.StoreTable;
import cat.nyaa.hmarket.gui.item.ButtonUIItem;
import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SignShopUI extends ShopUI {

    private final UUID owner;
    private static final int PLAYER_INVENTORY = 53;

    public SignShopUI(UUID owner, List<ItemCache> goods) {
        super(goods);
        this.owner = owner;
    }


    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (!clickType.equals(DataClickType.PICKUP)) {
            return;
        }
        // Player Inventory
        if (slotNum > PLAYER_INVENTORY) {
            return;
        }
        if (uiItemList.get(slotNum) == EmptyUIItem.EMPTY_UI_ITEM) {
            return;
        }
        if (uiItemList.get(slotNum) instanceof ButtonUIItem) {
            ((ButtonUIItem) uiItemList.get(slotNum)).onClick(clickType, player);
            return;
        }
        var target = goods.get(slotNum);
        if (!InventoryUtils.hasItem(player, target.nbt, 1)) {
            return;
        }
        // TODO: Eco check

        InventoryUtils.removeItem(player, target.nbt, 1);
        try {
            StoreTable.updateItem(new StoreModel(ItemStackUtils.itemToBase64(target.nbt), 1, owner.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
