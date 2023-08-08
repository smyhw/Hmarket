package cat.nyaa.hmarket.ui.data;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.HMarketAPI;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record MarketUiItemNamespacedKey(NamespacedKey itemId, NamespacedKey MarketId, NamespacedKey ItemTime,
                                        NamespacedKey ItemOwnerUniqueID) {
    static MarketUiItemNamespacedKey instance = null;

    public MarketUiItemNamespacedKey(@NotNull HMarketAPI api) {
        this(
                new NamespacedKey(api.getDatabaseManager().getPlugin(), "market_item_id"),
                new NamespacedKey(api.getDatabaseManager().getPlugin(), "market_market_id"),
                new NamespacedKey(api.getDatabaseManager().getPlugin(), "market_item_time"),
                new NamespacedKey(api.getDatabaseManager().getPlugin(), "market_item_owner_unique_id")
        );
    }

    @Nullable
    public static MarketUiItemNamespacedKey getInstanceNullable() {
        if (instance == null) {
            var api = Hmarket.getAPI();
            if (api != null) {
                instance = new MarketUiItemNamespacedKey(api);
            }
        }
        return instance;
    }

    public static Optional<MarketUiItemNamespacedKey> getInstance() {
        return Optional.ofNullable(getInstanceNullable());
    }
}
