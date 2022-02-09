package cat.nyaa.hmarket.shopitem;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.database.ItemTables;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.*;

public class ItemCacheManager {

    private static final class InstanceHolder {
        private static final ItemCacheManager instance = new ItemCacheManager();
    }

    private ItemCacheManager() {
    }

    public static ItemCacheManager getInstance() {
        return InstanceHolder.instance;
    }

    private Map<UUID, List<ItemCache>> itemCache = new HashMap<>();

    public void init() {

        try {
            var items = ItemTables.selectAll();
            assert items != null;
            for (var i : items) {
                if (itemCache.containsKey(i.owner)) {
                    itemCache.get(i.owner).add(i.toCache());
                } else {
                    itemCache.put(i.owner, Lists.newArrayList(i.toCache()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ItemCache> getItemCache(UUID owner, ShopItemType type) {
        var rs = itemCache.get(owner);
        if (rs != null) {
            return rs.stream().filter(i -> i.type.equals(type)).toList();
        }
        return new ArrayList<>();
    }

    public void addShopItem(ShopItem shopItem) {
        List<ItemCache> t;
        if (itemCache.containsKey(shopItem.getOwner())) {
            t = itemCache.get(shopItem.getOwner());
            t.add(shopItem.toCache());
        } else {
            t = new ArrayList<>();
            t.add(shopItem.toCache());
            itemCache.put(shopItem.getOwner(), t);
        }
    }
}
