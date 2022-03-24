package cat.nyaa.hmarket.utils;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.ui.HMPageUi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HMUiUtils {
    static ConcurrentHashMap<UUID, WeakReference<HMPageUi>> uiMap = new ConcurrentHashMap<>();

    private static @Nullable HMPageUi getUiNullable(UUID uuid) {
        uiMap.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().get() == null);
        if (uiMap.containsKey(uuid)) {
            var weakRef = uiMap.get(uuid);
            if (weakRef != null) {
                return weakRef.get();
            }
        }
        return null;
    }

    public static @Nullable HMPageUi getUi(UUID uuid) {
        var ui = getUiNullable(uuid);
        if (ui == null) {
            return createUi(uuid);
        }
        return ui;
    }

    private static @Nullable HMPageUi createUi(UUID shopId) {
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return null;
        if (Hmarket.uiManager == null) return null;
        var owner = Bukkit.getPlayer(shopId);
        String title;
        if (shopId.equals(hmApi.getSystemShopId())) {
            title = HMI18n.format("info.ui.title.shop.system");
        } else {
            title = HMI18n.format("info.ui.title.shop.user", owner == null ? shopId.toString() : owner.getName());
        }
        var ui = new HMPageUi(shopId, Hmarket.uiManager::broadcastFullState, title);
        uiMap.put(shopId, new WeakReference<>(ui));
        return ui;
    }

    public static void openShopUi(Player player, UUID shopId) {
        if (Hmarket.uiManager == null) return;
        var ui = getUi(shopId);
        if (ui == null) return;
        Hmarket.uiManager.sendOpenWindow(player, ui);
    }
}
