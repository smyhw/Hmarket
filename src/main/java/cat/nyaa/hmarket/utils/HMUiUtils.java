package cat.nyaa.hmarket.utils;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.ui.HMPageUi;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HMUiUtils {
    static ConcurrentHashMap<UUID, WeakReference<HMPageUi>> shopUiMap = new ConcurrentHashMap<>();

    private static @Nullable HMPageUi getShopUiNullable(UUID uuid) {
        shopUiMap.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().get() == null);
        if (shopUiMap.containsKey(uuid)) {
            var weakRef = shopUiMap.get(uuid);
            if (weakRef != null) {
                return weakRef.get();
            }
        }
        return null;
    }

    public static @Nullable HMPageUi getShopUi(UUID uuid) {
        var ui = getShopUiNullable(uuid);
        if (ui == null) {
            return createShopUi(uuid);
        }
        return ui;
    }

    private static @Nullable HMPageUi createShopUi(UUID shopId) {
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return null;
        if (Hmarket.uiManager == null) return null;

        String title;
        if (shopId.equals(MarketIdUtils.getSystemShopId())) {
            title = HMI18n.format("info.ui.title.shop.system");
        } else {
            title = HMI18n.format("info.ui.title.shop.user", PlayerNameUtils.getPlayerNameById(shopId));
        }
        var ui = new HMPageUi(shopId, Hmarket.uiManager::broadcastFullState, title);
        shopUiMap.put(shopId, new WeakReference<>(ui));
        return ui;
    }

    public static void openShopUi(Player player, UUID shopId) {
        if (Hmarket.uiManager == null) return;
        var ui = getShopUi(shopId);
        if (ui == null) return;
        Hmarket.uiManager.sendOpenWindow(player, ui);
    }

    public static void updateShopUi(UUID marketId) {
        var ui = getShopUiNullable(marketId);
        if (ui == null) return;
        ui.updateShopItem();
    }
}
