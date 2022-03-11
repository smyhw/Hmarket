package cat.nyaa.hmarket.api;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.hmarket.api.exception.InvalidItemIdException;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.data.ShopItemData;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import cat.nyaa.hmarket.utils.HMInventoryUtils;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HMarketAPI implements IMarketAPI {
    private final HmarketDatabaseManager databaseManager;
    private final EconomyCore economyCore;

    HMarketAPI(HmarketDatabaseManager databaseManager, EconomyCore economyCore) {
        this.databaseManager = databaseManager;
        this.economyCore = economyCore;
    }

    @Override
    public CompletableFuture<Optional<Integer>> offer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack items, double price) throws NotEnoughItemsException {
        if (InventoryUtils.hasItem(player, items, items.getAmount())) {
            throw new NotEnoughItemsException();
        }
        if (!InventoryUtils.removeItem(player, items, items.getAmount())) {
            throw new NotEnoughItemsException();
        }
        return databaseManager.addItemsToShop(items, player.getUniqueId(), marketId, price)
                .thenApply(itemId -> {
                    if (itemId.isEmpty()) {
                        HMInventoryUtils.giveOrDropItem(player, items);
                    }
                    return itemId;
                });

    }

    @Override
    public CompletableFuture<Boolean> buy(@NotNull Player player, int itemId, int amount) {
        return databaseManager.getShopItemData(itemId).thenApply(shopItemData -> {
                    if (shopItemData == null) {
                        return null;
                    }

                    return TaskUtils.async.callSyncAndGet(() -> {
                        ItemStack itemStack = ItemStackUtils.itemFromBase64(shopItemData.itemNbt());
                        if (itemStack.getAmount() < amount) {
                            return null;
                        }

                        if (UUID.fromString(shopItemData.owner()).equals(player.getUniqueId())) {//owner buy his own item
                            return shopItemData;
                        }

                        if (economyCore.getPlayerBalance(player.getUniqueId()) < shopItemData.price() * amount) {
                            return null;
                        }
                        return shopItemData;
                    });
                })
                .thenApply(shopItemData -> {
                    if (shopItemData == null) return false; // not enough money or item not exist
                    try {
                        return databaseManager.removeItemsFromShop(itemId, amount).thenApply((b) -> {
                            if (b) {
                                if (TaskUtils.async.callSyncAndGet(() -> {

                                    if (!UUID.fromString(shopItemData.owner()).equals(player.getUniqueId())) {
                                        if (!economyCore.withdrawPlayer(player.getUniqueId(), shopItemData.price() * amount)) {
                                            return false;// not owner and not enough money
                                        }
                                    }

                                    ItemStack itemStack = ItemStackUtils.itemFromBase64(shopItemData.itemNbt()).clone();
                                    itemStack.setAmount(amount);
                                    HMInventoryUtils.giveOrDropItem(player, itemStack);
                                    return true;
                                })) {
                                    //withdrawPlayer success
                                    return true;
                                } else {
                                    //withdrawPlayer failed
                                    databaseManager.addItemsToShop(ItemStackUtils.itemFromBase64(shopItemData.itemNbt()), UUID.fromString(shopItemData.owner()), UUID.fromString(shopItemData.market()), shopItemData.price());
                                    return false;
                                }
                            }
                            return false;
                        }).get();
                    } catch (CancellationException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    }
                });
    }

    @Override
    public CompletableFuture<Boolean> buy(Player player, ShopItemData itemData, int amount)  throws InvalidItemIdException, NotEnoughItemsException, NotEnoughMoneyException {
        return null;//todo
    }

    @Override
    public CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId) {
        return null;//todo
    }

    @Override
    public CompletableFuture<Boolean> updateShopItem(int itemId) {
        return null;//todo
    }


}
