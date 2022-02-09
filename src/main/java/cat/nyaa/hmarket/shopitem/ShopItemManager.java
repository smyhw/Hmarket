package cat.nyaa.hmarket.shopitem;

import cat.nyaa.hmarket.database.ItemTables;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

public class ShopItemManager {
    private static Plugin hmarket;
    private static final class InstanceHolder {
        private static final ShopItemManager instance = new ShopItemManager();
    }

    private ShopItemManager() {

    }

    public static void init(Plugin hmarket) {
        ShopItemManager.hmarket = hmarket;
    }

    public static ShopItemManager getInstance() {
        return InstanceHolder.instance;
    }

    public void addShopItem(ShopItem item) {
        ItemCacheManager.getInstance().addShopItem(item);
        Bukkit.getScheduler().runTaskAsynchronously(hmarket, () -> {
            try {
                ItemTables.insertItem(item.toItemModel());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
