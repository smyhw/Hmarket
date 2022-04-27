package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.api.data.MarketBuyResult;
import cat.nyaa.hmarket.api.data.MarketOfferResult;
import cat.nyaa.hmarket.db.data.ShopItemData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMarketAPI {

    CompletableFuture<MarketOfferResult> offer(Player player, UUID marketId, ItemStack items, double price);

    void commandOffer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack item, double price);

    CompletableFuture<MarketBuyResult> buy(Player player, UUID marketId, int itemId, int amount);

    void commandBuy(@NotNull Player player, @NotNull UUID marketId, int itemId, int amount);

    CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId);

    void updateItem(long begin, long now);

    double getFeeRate(@NotNull ShopItemData shopItemData);
}
