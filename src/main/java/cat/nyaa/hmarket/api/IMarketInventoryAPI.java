package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.data.MarketInventoryItemData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMarketInventoryAPI {
    CompletableFuture<Boolean> putItem(Player player, UUID marketId, ItemStack item);

    CompletableFuture<Boolean> takeItem(Player player, UUID marketId, int itemId);

    CompletableFuture<List<MarketInventoryItemData>> getItemList(UUID marketId);
}
