package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.data.ShopItemData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMarketAPI {
    CompletableFuture<Boolean> offer(Player player, UUID marketId, ItemStack items, double price);

    CompletableFuture<Boolean> buy(Player player,int itemId, int amount);

    CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId);
}
