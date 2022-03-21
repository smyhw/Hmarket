package cat.nyaa.hmarket.api;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.api.data.MarketOfferResult;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.config.HMConfig;
import cat.nyaa.hmarket.data.ShopItemData;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import cat.nyaa.hmarket.utils.HMInventoryUtils;
import cat.nyaa.hmarket.utils.TimeUtils;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HMarketAPI implements IMarketAPI {
    public static UUID systemShopId = UUID.nameUUIDFromBytes("HM_system_shop".getBytes());
    private final HmarketDatabaseManager databaseManager;
    private final EconomyCore economyCore;
    private final HMConfig config;

    public HMarketAPI(HmarketDatabaseManager databaseManager, EconomyCore economyCore, HMConfig config) {
        this.databaseManager = databaseManager;
        this.economyCore = economyCore;
        this.config = config;
    }

    @Override
    public UUID getSystemShopId() {
        return systemShopId;
    }

    @Override
    public CompletableFuture<MarketOfferResult> offer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack items, double price) {
        return hasEnoughSpace(marketId, player).thenApplyAsync(
                OptBool -> {
                    if (OptBool.isEmpty())
                        return MarketOfferResult.fail(MarketOfferResult.MarketOfferReason.DATABASE_ERROR);
                    if (!OptBool.get()) {
                        return MarketOfferResult.fail(MarketOfferResult.MarketOfferReason.NOT_ENOUGH_SPACE);
                    }
                    var reason = TaskUtils.async.callSyncAndGet(
                            () -> {
                                var fee = getListingFee(marketId);
                                if (price <= 0) {
                                    return MarketOfferResult.MarketOfferReason.INVALID_PRICE;
                                }
                                if (!InventoryUtils.hasItem(player, items, items.getAmount())) {
                                    return MarketOfferResult.MarketOfferReason.NOT_ENOUGH_ITEMS;
                                }
                                if (economyCore.getPlayerBalance(player.getUniqueId()) < fee) {
                                    return MarketOfferResult.MarketOfferReason.NOT_ENOUGH_MONEY;
                                }
                                if (!InventoryUtils.removeItem(player, items, items.getAmount())) {
                                    return MarketOfferResult.MarketOfferReason.NOT_ENOUGH_ITEMS;
                                }
                                if (!economyCore.withdrawPlayer(player.getUniqueId(), fee)) {
                                    return MarketOfferResult.MarketOfferReason.NOT_ENOUGH_MONEY;
                                } else {
                                    economyCore.depositSystemVault(fee);
                                    return MarketOfferResult.MarketOfferReason.SUCCESS;
                                }
                            }
                    );
                    if (reason != MarketOfferResult.MarketOfferReason.SUCCESS) {
                        return MarketOfferResult.fail(reason);
                    }
                    try {
                        var OptItemId = databaseManager.addItemsToShop(items, items.getAmount(), player.getUniqueId(), marketId, price)
                                .thenApply(itemId -> {
                                    if (itemId.isEmpty()) {
                                        HMInventoryUtils.giveOrDropItem(player, items);
                                    }
                                    return itemId;
                                }).get();
                        if (OptItemId.isEmpty())
                            return MarketOfferResult.fail(MarketOfferResult.MarketOfferReason.DATABASE_ERROR);
                        return MarketOfferResult.success(OptItemId.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return MarketOfferResult.fail(MarketOfferResult.MarketOfferReason.TASK_FAILED);
                }
        ).thenApply((MarketOfferResult result) -> {
            if (result.isSuccess() || result.itemId().isPresent()) onShopOffer(player, marketId, items, price);
            return result;
        });

    }

    public CompletableFuture<Optional<Boolean>> hasEnoughSpace(UUID marketId, Player player) {
        if (marketId.equals(systemShopId)) {
            return databaseManager.getShopItemCountByOwner(marketId, player.getUniqueId()).thenApply((optCount) -> TaskUtils.async.callSyncAndGet(
                    () -> {
                        if (optCount.isEmpty()) return Optional.empty();
                        var count = optCount.get();
                        if (count < config.limitSlotsMarket) {
                            return Optional.of(true);
                        }
                        return Optional.of(false);
                    }
            ));
        } else {
            return databaseManager.getShopAllItemCount(marketId).thenApply((optCount) -> TaskUtils.async.callSyncAndGet(
                    () -> {
                        if (optCount.isEmpty()) return Optional.empty();
                        var count = optCount.get();
                        if (count < config.limitSlotsSignshopSell) {
                            return Optional.of(true);
                        }
                        return Optional.of(false);
                    }
            ));
        }
    }

    private void onShopOffer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack items, double price) {
        HMI18n.send(player, "info.market.sellfee", getListingFee(marketId));
        if (marketId.equals(systemShopId)) {
            new Message(HMI18n.format("info.market.sell", player)).append(items).broadcast();
        }
    }

    public double getListingFee(@NotNull UUID marketId) {
        if (marketId.equals(systemShopId)) {
            return config.feeMarket;
        }
        return config.feeSignshop;
    }

    public double getFeeRate(@NotNull UUID marketId) {
        if (marketId.equals(systemShopId)) {
            return config.taxMarket / 100.0;
        }
        return config.taxSignshop / 100.0;
    }


    public double getFeeRate(@NotNull ShopItemData shopItemData) {
        return getFeeRate(UUID.fromString(shopItemData.market()));
    }

    @Override
    public CompletableFuture<Boolean> buy(@NotNull Player player, int itemId, int amount) {
        return databaseManager.getShopItemData(itemId).thenApply(shopItemData -> {
                    if (shopItemData == null) {
                        return null;
                    }

                    return TaskUtils.async.callSyncAndGet(() -> {
                        try {
                            if (checkBuy(shopItemData, player, amount)) {
                                return shopItemData;
                            }
                        } catch (Exception ignored) {
                        }
                        return null;
                    });
                })
                .thenApplyAsync(shopItemData -> {
                    if (shopItemData == null) return false; // not enough money or item not exist
                    try {
                        return databaseManager.removeItemsFromShop(itemId, amount).thenApply((b) -> {
                            if (b) {
                                if (TaskUtils.async.callSyncAndGet(() -> {
                                    if (!UUID.fromString(shopItemData.owner()).equals(player.getUniqueId())) {
                                        var tr = economyCore.playerTrade(player.getUniqueId(), UUID.fromString(shopItemData.owner()), shopItemData.price() * amount, getFeeRate(shopItemData));
                                        if (!tr.isSuccess()) {
                                            return false;
                                        }
                                    }
                                    ItemStack itemStack = ItemStackUtils.itemFromBase64(shopItemData.itemNbt()).clone();
                                    itemStack.setAmount(amount);
                                    HMInventoryUtils.giveOrDropItem(player, itemStack);
                                    return true;
                                })) {
                                    //playerTrade success
                                    return true;
                                } else {
                                    //playerTrade failed
                                    databaseManager.addItemsToShop(ItemStackUtils.itemFromBase64(shopItemData.itemNbt()), amount, UUID.fromString(shopItemData.owner()), UUID.fromString(shopItemData.market()), shopItemData.price());
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

    private boolean checkBuy(@NotNull ShopItemData shopItemData, Player player, int amount) throws NotEnoughMoneyException, NotEnoughItemsException {
        if (shopItemData.amount() < amount) {
            throw new NotEnoughItemsException();
        }

        if (UUID.fromString(shopItemData.owner()).equals(player.getUniqueId())) {//owner buy his own item
            return true;
        }

        if (economyCore.getPlayerBalance(player.getUniqueId()) < (shopItemData.price() * amount) * (1.0 + getFeeRate(shopItemData))) {
            throw new NotEnoughMoneyException();
        }
        return true;
    }

    @Override
    public CompletableFuture<Boolean> buy(Player player, ShopItemData itemData, int amount) throws NotEnoughMoneyException, NotEnoughItemsException {
        if (checkBuy(itemData, player, amount)) {
            return buy(player, itemData.itemId(), amount);
        }
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId) {
        return databaseManager.getAllShopItems(marketId).thenApplyAsync(shopItemDataList -> {
            var result = new ArrayList<>(shopItemDataList);
            result.removeIf((shopItemData) -> {
                if (shopItemData.amount() <= 0) {
                    try {
                        if (databaseManager.removeShopItem(shopItemData.itemId()).get() > 0)
                            return true;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            });
            return result;
        });
    }

    public void updateItem(long begin, long now) {
        if (begin >= now) return;
        databaseManager.getNeedUpdateItems(begin).thenAcceptAsync(shopItemDataList -> shopItemDataList.forEach(shopItemData -> {
            var keepItems = true;
            if (UUID.fromString(shopItemData.market()).equals(systemShopId)) {
                keepItems = storageFee(TimeUtils.getUnixTimeStampNow(), config.storageMarketFreedays, config.storageMarketBase, config.storageMarketPercent, shopItemData);
            } else {
                keepItems = storageFee(TimeUtils.getUnixTimeStampNow(), config.storageSignshopFreedays, config.storageSignshopBase, config.storageSignshopPercent, shopItemData);
            }
            if (!keepItems) {
                databaseManager.removeShopItem(shopItemData.itemId());
            } else {
                databaseManager.setItemUpdateTime(shopItemData.itemId(), now);
            }
        }));
    }

    private boolean storageFee(long now, double freeDays, double base, double percent, ShopItemData shopItemData) {
        return TaskUtils.async.callSyncAndGet(() -> {
                    var lastUpdateAt = shopItemData.updatedAt();
                    var createdAt = shopItemData.createdAt();
                    var lastDays = Math.floor((lastUpdateAt - createdAt) / (1000.0 * 60.0 * 60.0 * 24.0));
                    var nowDays = Math.floor((now - createdAt) / (1000.0 * 60.0 * 60.0 * 24.0));
                    if (nowDays <= lastDays) return true;

                    if ((now - createdAt) < ((1000.0 * 60.0 * 60.0 * 24.0 * freeDays))) {
                        return true;
                    }
                    var billableDays = nowDays - lastDays;
                    var fee = (base + percent * shopItemData.price()) * billableDays;
                    var balance = economyCore.getPlayerBalance(UUID.fromString(shopItemData.owner()));
                    if (economyCore.withdrawPlayer(UUID.fromString(shopItemData.owner()), fee)) {
                        economyCore.depositSystemVault(fee);
                        return true;
                    }
                    return !(balance < fee);
                }
        );
    }
}

