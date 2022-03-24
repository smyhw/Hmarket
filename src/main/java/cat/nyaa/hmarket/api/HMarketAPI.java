package cat.nyaa.hmarket.api;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.ecore.ServiceFeePreference;
import cat.nyaa.ecore.TransactionStatus;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.data.MarketBuyResult;
import cat.nyaa.hmarket.api.data.MarketItemDataResult;
import cat.nyaa.hmarket.api.data.MarketOfferResult;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class HMarketAPI implements IMarketAPI {
    public static UUID systemShopId = UUID.nameUUIDFromBytes("HM_system_shop".getBytes());
    public static UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final HmarketDatabaseManager databaseManager;
    private final EconomyCore economyCore;
    private final HMConfig config;

    public HMarketAPI(HmarketDatabaseManager databaseManager, EconomyCore economyCore, HMConfig config) {
        this.databaseManager = databaseManager;
        this.economyCore = economyCore;
        this.config = config;
    }

    public static UUID parseMarketId(@NotNull String marketId) {
        if (marketId.equalsIgnoreCase("system")) {
            return systemShopId;
        } else {
            return parseUUID(marketId);
        }
    }

    public static UUID parseUUID(@NotNull String stringUUID) {
        UUID result = null;
        try {
            result = UUID.fromString(stringUUID);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (result == null) {
            logWarning("can't parse id: " + stringUUID);
            return getUnknownId();
        }
        return result;
    }

    private static UUID getUnknownId() {
        logWarning("get unknown id");
        return unknownId;
    }

    public static void logInfo(String message) {
        var plugin = Hmarket.getInstance();
        if (plugin == null) return;
        TaskUtils.async.callSync(() -> plugin.getLogger().info(message));
    }

    public static void logWarning(String message) {
        var plugin = Hmarket.getInstance();

        TaskUtils.async.callSync(() -> {
            if (plugin != null) {
                plugin.getLogger().warning(message);
            } else {
                Logger.getAnonymousLogger().warning(message);
            }
        });
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
                        return MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.DATABASE_ERROR);
                    if (!OptBool.get()) {
                        return MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_SPACE);
                    }
                    var reason = TaskUtils.async.callSyncAndGet(
                            () -> {
                                var fee = getListingFee(marketId);
                                if (price <= 0) {
                                    return MarketOfferResult.MarketOfferStatus.INVALID_PRICE;
                                }
                                if (!InventoryUtils.hasItem(player, items, items.getAmount())) {
                                    return MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_ITEMS;
                                }
                                if (economyCore.getPlayerBalance(player.getUniqueId()) < fee) {
                                    return MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_MONEY;
                                }
                                if (!InventoryUtils.removeItem(player, items, items.getAmount())) {
                                    return MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_ITEMS;
                                }
                                if (!economyCore.withdrawPlayer(player.getUniqueId(), fee)) {
                                    return MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_MONEY;
                                } else {
                                    economyCore.depositSystemVault(fee);
                                    return MarketOfferResult.MarketOfferStatus.SUCCESS;
                                }
                            }
                    );
                    if (reason != MarketOfferResult.MarketOfferStatus.SUCCESS) {
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
                            return MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.DATABASE_ERROR);
                        return MarketOfferResult.success(OptItemId.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.TASK_FAILED);
                }
        ).thenApply((MarketOfferResult result) -> {
            if (result.isSuccess() || result.itemId().isPresent()) onShopOffer(player, marketId, items, price);
            return result;
        });

    }

    public CompletableFuture<Optional<Boolean>> hasEnoughSpace(@NotNull UUID marketId, Player player) {
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
        logInfo("Player " + player.getName() + " has offered " + items.getType() + " * " + items.getAmount() + " for " + price + " to market " + marketId);
        HMI18n.send(player, "info.market.sellfee", getListingFee(marketId));
        if (marketId.equals(systemShopId)) {
            new Message(HMI18n.format("info.market.sell", player.getName())).append(items).broadcast();
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
        return getFeeRate(shopItemData.market());
    }

    public CompletableFuture<MarketItemDataResult> getShopItemData(int itemId) {
        return databaseManager.getShopItemData(itemId).thenApply(optShopItemData -> {
            if (optShopItemData.isEmpty()) {
                return MarketItemDataResult.fail(MarketItemDataResult.MarketItemDataStatus.NOT_FOUND);
            }
            return MarketItemDataResult.success(optShopItemData.get());
        });
    }

    @Override
    public CompletableFuture<MarketBuyResult> buy(@NotNull Player player, int itemId, int amount) {
        return getShopItemData(itemId).thenApplyAsync(result -> {
            if (!result.isSuccess()) return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.ITEM_NOT_FOUND);
            var OptShopItemData = result.data();
            if (OptShopItemData.isEmpty()) return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.ITEM_NOT_FOUND);
            var shopItemData = OptShopItemData.get();

            var checkBuy = TaskUtils.async.callSyncAndGet(() -> {
                if (shopItemData.amount() < amount) {
                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.OUT_OF_STOCK);
                }

                if (shopItemData.owner().equals(player.getUniqueId())) {//owner buy his own item
                    return MarketBuyResult.success();
                }

                if (economyCore.getPlayerBalance(player.getUniqueId()) < (shopItemData.price() * amount) * (1.0 + getFeeRate(shopItemData))) {
                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.NOT_ENOUGH_MONEY);
                }
                return MarketBuyResult.success();
            });
            if (!checkBuy.isSuccess()) {
                return checkBuy;
            }

            try {
                return databaseManager.removeItemsFromShop(itemId, amount).thenApply((b) -> {
                    if (b.isEmpty() || !b.get()) {
                        return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.CANNOT_REMOVE_ITEM);
                    }
                    var trade = TaskUtils.async.callSyncAndGet(() -> {
                        if (!shopItemData.owner().equals(player.getUniqueId())) {
                            var tr = economyCore.playerTrade(player.getUniqueId(), shopItemData.owner(), shopItemData.price() * amount, getFeeRate(shopItemData), ServiceFeePreference.ADDITIONAL);
                            if (!tr.isSuccess()) {
                                if (tr.status() == TransactionStatus.INSUFFICIENT_BALANCE) {
                                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.NOT_ENOUGH_MONEY);
                                } else {
                                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.TRANSACTION_ERROR);
                                }
                            }
//                        logInfo("The player bought an item from the market,Receipt：" + tr.getReceipt());
                        }
                        ItemStack itemStack = ItemStackUtils.itemFromBase64(shopItemData.itemNbt()).clone();
                        itemStack.setAmount(amount);
                        HMInventoryUtils.giveOrDropItem(player, itemStack);
                        return MarketBuyResult.success();
                    });
                    if (!trade.isSuccess()) {
                        logInfo("The player bought an item from the market,but the transaction failed.Item being returned to the shop.");
                        databaseManager.addItemsToShop(ItemStackUtils.itemFromBase64(shopItemData.itemNbt()), amount, shopItemData.owner(), shopItemData.market(), shopItemData.price()).thenAccept(
                                (OptItemId) -> {
                                    if (OptItemId.isEmpty()) {
                                        logWarning("The item was not added to the shop.");
                                    } else {
                                        logInfo("new item id:" + OptItemId.get());
                                    }
                                }
                        );
                        trade.setModified(true);
                        return trade;
                    }
                    return trade;
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.TASK_FAILED);
            }

        });
    }

    @Override
    public CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId) {
        return databaseManager.getAllShopItems(marketId).thenApplyAsync(shopItemDataList -> {
            List<ShopItemData> result = new ArrayList<>();
            shopItemDataList.ifPresent(result::addAll);
            result.removeIf((shopItemData) -> {
                if (shopItemData.amount() <= 0) {
                    try {
                        var optRemove = databaseManager.removeShopItem(shopItemData.itemId()).get();
                        if (optRemove.isPresent() && optRemove.get() > 0)
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

    @Contract(pure = true)
    private double getMarketStorageFeeRate(@NotNull UUID marketId) {
        if (marketId.equals(systemShopId)) {
            return (config.storageMarketPercent / 100.0d);
        } else {
            return (config.storageSignshopPercent / 100.0d);
        }
    }

    @Contract(pure = true)
    private double getMarketStorageFeeBase(@NotNull UUID marketId) {
        if (marketId.equals(systemShopId)) {
            return config.storageMarketBase;
        } else {
            return config.storageSignshopBase;
        }
    }

    @Contract(pure = true)
    private int getMarketStorageFreeDays(@NotNull UUID marketId) {
        if (marketId.equals(systemShopId)) {
            return config.storageMarketFreedays;
        } else {
            return config.storageSignshopFreedays;
        }
    }

    public void updateItem(long begin, long now) {
        if (begin >= now) return;
        databaseManager.getNeedUpdateItems(begin).thenAcceptAsync(shopItemDataList -> {
            if (shopItemDataList.isEmpty()) return;
            shopItemDataList.get().forEach(shopItemData -> {
                var marketId = shopItemData.market();
                var keepItems = true;
                keepItems = storageFee(TimeUtils.getUnixTimeStampNow(), getMarketStorageFreeDays(marketId), getMarketStorageFeeBase(marketId), getMarketStorageFeeRate(marketId), shopItemData);
                if (!keepItems) {
                    databaseManager.removeShopItem(shopItemData.itemId()).thenAccept(
                            (result) -> {
                                if (result.isEmpty() || result.get() <= 0) {
                                    logWarning("can't remove item: " + shopItemData.itemId() + " in shop " + shopItemData.market());
                                } else {
                                    logInfo("Item " + shopItemData.itemId() + " removed from shop " + shopItemData.market());
                                }

                            }
                    );
                    logInfo("Item " + shopItemData.itemId() + " removed from shop " + shopItemData.market());
                } else {
                    databaseManager.setItemUpdateTime(shopItemData.itemId(), now).thenAccept(
                            (result) -> {
                                if (result.isEmpty() || result.get() <= 0) {
                                    logWarning("can't update item " + shopItemData.itemId() + " in shop " + shopItemData.market());
                                }
                            }
                    );
                }
            });
        });
    }

    private boolean storageFee(long now, double freeDays, double base, double rate, ShopItemData shopItemData) {
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
                    var fee = (base + rate * shopItemData.price()) * billableDays;
                    var balance = economyCore.getPlayerBalance(shopItemData.owner());
                    if (economyCore.withdrawPlayer(shopItemData.owner(), fee)) {
                        economyCore.depositSystemVault(fee);
                        logInfo("[item update][" + TimeUtils.getUnixTimeStampNow() + "] " + shopItemData.owner() + " paid " + fee + " for storage fee. shop id:" + shopItemData.market());
                        return true;
                    }
                    logInfo("[item update][" + TimeUtils.getUnixTimeStampNow() + "] " + shopItemData.owner() + " not enough money to pay storage fee(" + fee + "). shop id:" + shopItemData.market());
                    return !(balance < fee);
                }
        );
    }
}

