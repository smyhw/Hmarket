package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.data.ShopItemData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMarketAPI {
    UUID getSystemShopId();

    CompletableFuture<Optional<Integer>> offer(Player player, UUID marketId, ItemStack items, double price) throws NotEnoughItemsException;

    CompletableFuture<Boolean> buy(Player player, int itemId, int amount);

    CompletableFuture<Boolean> buy(Player player, ShopItemData itemData, int amount) throws NotEnoughMoneyException, NotEnoughItemsException;

    CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId);

    CompletableFuture<Integer> updateShopItem();
}
