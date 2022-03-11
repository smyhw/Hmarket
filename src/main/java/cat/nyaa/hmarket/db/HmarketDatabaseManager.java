package cat.nyaa.hmarket.db;

import cat.nyaa.hmarket.data.ShopItemData;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HmarketDatabaseManager {
    HmarketDatabaseManager(){

    }

    public CompletableFuture<ShopItemData> getShopItemData(int itemId) {
        //todo
    }

    public CompletableFuture<Boolean> removeItemsFromShop(int itemId, int amount) {
        //todo
    }

    public CompletableFuture<Optional<Integer>> addItemsToShop(ItemStack items, UUID uniqueId, UUID marketId, double price) {
        //todo
    }
}
