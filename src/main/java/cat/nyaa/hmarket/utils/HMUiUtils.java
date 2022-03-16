package cat.nyaa.hmarket.utils;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.ui.HMPageUi;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HMUiUtils {
    public static void openShopUi(Player player, UUID shopId, String title) {
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return;
        if (Hmarket.uiManager == null) return;
        Hmarket.uiManager.sendOpenWindow(player, new HMPageUi(shopId, Hmarket.uiManager::broadcastChanges, title));
    }
}
