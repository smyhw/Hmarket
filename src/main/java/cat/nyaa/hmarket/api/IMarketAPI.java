package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.api.data.MarketBuyResult;
import cat.nyaa.hmarket.api.data.MarketOfferResult;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.api.exception.NotEnoughSpaceException;
import cat.nyaa.hmarket.data.ShopItemData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMarketAPI {
    UUID getSystemShopId();

    CompletableFuture<MarketOfferResult> offer(Player player, UUID marketId, ItemStack items, double price) throws NotEnoughItemsException, NotEnoughMoneyException, NotEnoughSpaceException;

    CompletableFuture<MarketBuyResult> buy(Player player, int itemId, int amount);

    CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId);

}
