package cat.nyaa.hmarket.utils;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.ui.HMPageUi;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HMUiUtils {
    public static void openShopUi(Player player, UUID shopId) {
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return;
        if (Hmarket.uiManager == null) return;
        var title = "SHOP";
        if (shopId.equals(hmApi.getSystemShopId())) {
            title = HMI18n.format("info.ui.title.shop.system");
        } else {
            title = HMI18n.format("info.ui.title.shop.user", player.getName());
        }
        Hmarket.uiManager.sendOpenWindow(player, new HMPageUi(shopId, Hmarket.uiManager::broadcastChanges, title));
    }
}
