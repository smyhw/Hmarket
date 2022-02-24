package cat.nyaa.hmarket.shopitem;

import cat.nyaa.hmarket.database.tables.ItemTables;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.*;

public class ItemCacheManager {

    private Plugin hmarket;

    private static final class InstanceHolder {
        private static final ItemCacheManager instance = new ItemCacheManager();
    }

    private ItemCacheManager() {
    }

    public static ItemCacheManager getInstance() {
        return InstanceHolder.instance;
    }

    private final Map<UUID, List<ItemCache>> itemCache = new HashMap<>();
    private final List<ItemCache> mallItemCache = new LinkedList<>();

    public void init(Plugin hmarket) {
        this.hmarket = hmarket;
        try {
            var items = ItemTables.selectAll();
            assert items != null;
            for (var i : items) {
                var type = ShopItemType.valueOf(i.type);
                if (type == ShopItemType.Mall) {
                    mallItemCache.add(i.toCacheWithOwner());
                } else {
                    if (itemCache.containsKey(i.owner)) {
                        itemCache.get(i.owner).add(i.toCache());
                    } else {
                        itemCache.put(i.owner, Lists.newArrayList(i.toCache()));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ItemCache> getSignItemCache(UUID owner, ShopItemType type) {
        var rs = itemCache.get(owner);
        if (rs != null) {
            return rs.stream().filter(i -> i.type.equals(type)).toList();
        }
        return new ArrayList<>();
    }

    public List<ItemCache> getMallItemCache() {
        return mallItemCache;
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

    public void syncDirty() {
        var dirty = itemCache.values().stream()
                .flatMap(List::stream)
                .filter(i -> i.isDirty)
                .toList();
        var dirty1 = mallItemCache.stream().filter(i -> i.isDirty).toList();
        dirty.addAll(dirty1);
        Bukkit.getScheduler().runTaskAsynchronously(hmarket, () -> {
            ItemTables.syncDirty(dirty);
            for (var item : dirty) {
                item.isDirty = false;
            }
        });
    }
}
