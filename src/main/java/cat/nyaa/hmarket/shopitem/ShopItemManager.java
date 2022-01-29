package cat.nyaa.hmarket.shopitem;

import cat.nyaa.hmarket.database.ItemTables;

import java.sql.SQLException;

public class ShopItemManager {
    private static final class InstanceHolder {
        private static final ShopItemManager instance = new ShopItemManager();
    }

    private ShopItemManager() {

    }

    public static ShopItemManager getInstance() {
        return InstanceHolder.instance;
    }

    public void addShopItem(ShopItem item) {
        try {
            ItemTables.insertItem(item.toItemModel());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
