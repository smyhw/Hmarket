package cat.nyaa.hmarket.api.impl;

import cat.nyaa.aolib.message.AoMessage;
import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.api.IMarketAPI;
import cat.nyaa.hmarket.api.data.MarketBuyResult;
import cat.nyaa.hmarket.api.data.MarketItemDataResult;
import cat.nyaa.hmarket.api.data.MarketOfferResult;
import cat.nyaa.hmarket.db.data.ShopItemData;
import cat.nyaa.hmarket.utils.*;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.Pair;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.util.concurrent.AtomicDouble;
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

public class MarketImpl implements IMarketAPI {
    private final HMarketAPI marketApi;

    public MarketImpl(HMarketAPI marketApi) {
        this.marketApi = marketApi;

    }

    @Override
    public CompletableFuture<MarketOfferResult> offer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack items, double price) {
        var fee = getListingFee(marketId);
        var ownerId = player.getUniqueId();
        if (price <= 0 || price >= Integer.MAX_VALUE) {
            return CompletableFuture.completedFuture(MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.INVALID_PRICE));
        }
        if (!InventoryUtils.hasItem(player, items, items.getAmount())) {
            return CompletableFuture.completedFuture(MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_ITEMS));
        }
        if (marketApi.getEconomyCore().getPlayerBalance(ownerId) < fee) {
            return CompletableFuture.completedFuture(MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_MONEY));
        }
        if (!InventoryUtils.removeItem(player, items, items.getAmount())) {
            return CompletableFuture.completedFuture(MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_ITEMS));
        }
        if (!marketApi.getEconomyCore().withdrawPlayer(ownerId, fee)) {
            return CompletableFuture.completedFuture(MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_MONEY));
        }
        var limit = marketId.equals(MarketIdUtils.getSystemShopId()) ? marketApi.getConfig().limitSlotsMarket : marketApi.getConfig().limitSlotsSignshopSell;
        return marketApi.getDatabaseManager().addItemToShop(items, items.getAmount(), ownerId,
                        marketId, price, limit)
                .thenApply(
                        optionalItemId -> {
                            if (optionalItemId.isEmpty()) {
                                return MarketOfferResult.fail(MarketOfferResult.MarketOfferStatus.NOT_ENOUGH_SPACE);
                            }
                            return MarketOfferResult.success(optionalItemId.get());
                        }
                )
                .thenComposeAsync((result) -> TaskUtils.async.runSyncMethod(() -> {
                    if (result.isSuccess()) {
                        onShopOffer(player, marketId, items, price);
                        marketApi.getEconomyCore().depositSystemVault(fee);
                    } else {
                        HMInventoryUtils.giveOrDropItem(player, items);
                        marketApi.getEconomyCore().depositPlayer(ownerId, fee);
                    }
                    return result;
                }));
    }

    public void commandOffer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack item, double price) {
        var playerId = player.getUniqueId();
        offer(player, marketId, item, price).thenAccept(
                (result) -> {
                    switch (result.status()) {
                        case SUCCESS ->
                                HMI18n.sendPlayerSync(playerId, "command.offer.success", result.itemId().orElse(-1));
                        case NOT_ENOUGH_ITEMS -> HMI18n.sendPlayerSync(playerId, "command.not-enough-item-in-hand");
                        case NOT_ENOUGH_MONEY -> HMI18n.sendPlayerSync(playerId, "command.not-enough-money");
                        case NOT_ENOUGH_SPACE -> HMI18n.sendPlayerSync(playerId, "command.not-enough-space");
                        case TASK_FAILED -> HMI18n.sendPlayerSync(playerId, "command.task-failed");
                        case DATABASE_ERROR -> HMI18n.sendPlayerSync(playerId, "command.database-error");
                        case INVALID_PRICE -> HMI18n.sendPlayerSync(playerId, "command.invalid-price");
                    }
                }
        );
    }

    public CompletableFuture<Optional<Boolean>> hasEnoughSpace(@NotNull UUID marketId, Player player) {
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            return marketApi.getDatabaseManager().getShopItemCountByOwner(marketId, player.getUniqueId()).thenApply((optCount) -> TaskUtils.async.getSync(
                    () -> {
                        if (optCount.isEmpty()) return null;
                        var count = optCount.get();
                        return count < marketApi.getConfig().limitSlotsMarket;
                    })
            );
        } else {
            return marketApi.getDatabaseManager().getShopAllItemCount(marketId).thenApply((optCount) -> TaskUtils.async.getSync(
                    () -> {
                        if (optCount.isEmpty()) return null;
                        var count = optCount.get();
                        return count < marketApi.getConfig().limitSlotsSignshopSell;
                    }
            ));
        }
    }

    private void onShopOffer(@NotNull Player player, @NotNull UUID marketId, @NotNull ItemStack items, double price) {
        HMLogUtils.logInfo("Player " + player.getName() + " has offered " + items.getType() + " * " + items.getAmount() + " for " + price + " to market " + marketId);
        HMI18n.send(player, "info.market.sellfee", getListingFee(marketId));
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            new Message(HMI18n.format("info.market.sell", player.getName())).append(items).broadcast();
        }
        HMUiUtils.updateShopUi(marketId);
    }


    @Override
    public CompletableFuture<MarketBuyResult> buy(@NotNull Player player, UUID marketId, int itemId, int amount) {
        var playerId = player.getUniqueId();
        var paidCost = new AtomicDouble(0d);
        var paidTax = new AtomicDouble(0d);
        return marketApi.getDatabaseManager().getShopItemData(itemId).thenApplyAsync(optionalShopItemData -> {
            if (optionalShopItemData.isEmpty())
                return Pair.of(MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.ITEM_NOT_FOUND), null);
            ShopItemData shopItemData = optionalShopItemData.get();

            if (!shopItemData.market().equals(marketId)) {
                return Pair.of(MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.WRONG_MARKET), shopItemData);
            }
            if (shopItemData.amount() < amount) {// check amount
                return Pair.of(MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.OUT_OF_STOCK), shopItemData);
            }
            if (shopItemData.owner().equals(playerId)) {
                return Pair.of(MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.PLAYER_OWNS_ITEM), shopItemData);
            }

            return Pair.of(TaskUtils.async.getSyncDefault(() -> {
                var cost = shopItemData.price() * amount;
                var tax = cost * getTaxRate(shopItemData);
                if (marketApi.getEconomyCore().getPlayerBalance(playerId) < cost + tax) {
                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.NOT_ENOUGH_MONEY);
                }
                if (!marketApi.getEconomyCore().withdrawPlayer(playerId, cost + tax)) {
                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.TRANSACTION_ERROR);
                }
                paidCost.set(cost);
                paidTax.set(tax);


                return MarketBuyResult.success();
            }, MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.TASK_FAILED)), shopItemData);


        }).thenCompose(pair -> {
            ShopItemData shopItemData = (cat.nyaa.hmarket.db.data.ShopItemData) pair.getValue();

            if (shopItemData == null) {
                return CompletableFuture.completedFuture(MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.ITEM_NOT_FOUND));
            }

            if (!pair.getKey().isSuccess()) {
                if (pair.getKey().status() == MarketBuyResult.MarketBuyStatus.PLAYER_OWNS_ITEM) {// withdraw item
                    return this.withdrawItem(player, shopItemData, amount);
                }
                return CompletableFuture.completedFuture(pair.getKey());
            }


            //remove item from market and add to player inventory
            return marketApi.getDatabaseManager().buyItemFromMarket(marketId, itemId, amount, shopItemData.price(), shopItemData.itemNbt()).thenApplyAsync((b) -> {
                if (b.isEmpty() || !b.get()) {
                    if (paidCost.get() > 0 || paidTax.get() > 0) {
                        if (!TaskUtils.async.getSyncDefault(() -> marketApi.getEconomyCore().depositPlayer(playerId, paidCost.get() + paidTax.get()), false)) {
                            HMLogUtils.logWarning("Transaction Failed:Player " + playerId + " refund failed");
                            HMLogUtils.logWarning("cosy: " + paidCost.get() + ",tax:" + paidCost.get());
                        }
                    }
                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.CANNOT_BUY_ITEM);
                }
                var itemResult = TaskUtils.async.getSyncDefault(() -> {
                    ItemStack itemStack = giveItemAndUpdateShopSync(player, shopItemData.itemNbt(), marketId, amount);
                    if (paidTax.get() > 0) {
                        if (!marketApi.getEconomyCore().depositSystemVault(paidTax.get())) {
                            HMLogUtils.logWarning("Transaction Failed:Failed to deposit system vault");
                            HMLogUtils.logWarning("cost: " + paidTax.get());
                            HMLogUtils.logWarning("from: " + playerId);
                        }
                    }
                    if (paidCost.get() > 0) {
                        if (!marketApi.getEconomyCore().depositPlayer(shopItemData.owner(), paidCost.get())) {
                            HMLogUtils.logWarning("Transaction Failed:Failed to deposit player " + shopItemData.owner());
                            HMLogUtils.logWarning("cost: " + (paidCost.get()));
                            HMLogUtils.logWarning("from: " + playerId);
                        }
                    }
                    onShopSold(player, shopItemData, itemStack, amount, paidCost.get(), paidTax.get());
                    HMLogUtils.logInfo("Player " + playerId + " bought " + itemStack + " from market " + marketId);
                    HMLogUtils.logInfo("cost: " + paidCost.get() + " tax: " + paidTax.get() + "," + shopItemData.owner() + " received: " + (paidCost.get() - paidTax.get()));
                    return true;
                }, false);
                if (!itemResult) {
                    HMLogUtils.logWarning("Failed to give item to player " + player.getUniqueId() + " : Task failed");
                    HMLogUtils.logWarning("item data : " + shopItemData);
                    return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.TASK_FAILED);
                }
                return MarketBuyResult.success();

            });

        });


    }

    private @NotNull ItemStack giveItemAndUpdateShopSync(@NotNull Player player, @NotNull String itemNbt, @NotNull UUID marketId, int amount) {
        ItemStack itemStack = ItemStackUtils.itemFromBase64(itemNbt).clone();
        itemStack.setAmount(amount);
        HMInventoryUtils.giveOrDropItem(player, itemStack);
        HMUiUtils.updateShopUi(marketId);
        return itemStack;
    }

    private CompletableFuture<MarketBuyResult> withdrawItem(@NotNull Player player, @NotNull ShopItemData shopItemData, int amount) {
        var playerId = player.getUniqueId();
        return marketApi.getDatabaseManager().withdrawItemFromMarket(
                        shopItemData.market(), playerId, shopItemData.itemId(), amount, shopItemData.itemNbt()
                )
                .thenApply(
                        withdrawResult -> {
                            if (withdrawResult.isEmpty() || !withdrawResult.get()) {
                                return MarketBuyResult.fail(MarketBuyResult.MarketBuyStatus.CANNOT_BUY_ITEM);
                            }
                            giveItemAndUpdateShopSync(player, shopItemData.itemNbt(), shopItemData.market(), amount);
                            return MarketBuyResult.success(true);
                        }
                );


    }

    private void onShopSold(Player player, ShopItemData shopItemData, ItemStack itemStack, int amount, double paidCost, double paidTax) {
        AoMessage.getInstanceOptional().ifPresent(
                aoMessage -> aoMessage.sendMessageTo(
                        player.getUniqueId(),
                        HMI18n.format(
                                "info.market.sold",
                                itemStack.hasItemMeta()
                                        && itemStack.getItemMeta() != null
                                        && itemStack.getItemMeta().hasDisplayName() ?
                                        itemStack.getItemMeta().getDisplayName() : itemStack.getType().name(),

                                amount,
                                player.getName(),
                                paidCost + paidTax
                        )
                )
        );
    }

//    private void onShopSold(UUID marketId, int itemId, ItemStack itemStack, int amount, double price) {

    //    }
    @Override
    public void commandBuy(@NotNull Player player, @NotNull UUID marketId, int itemId, int amount) {
        buy(player, marketId, itemId, amount).thenAccept((marketBuyResult) -> {
            switch (marketBuyResult.status()) {
                case SUCCESS -> HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.buy_success");
                case WITHDRAW_SUCCESS -> HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.withdraw_success");
                case OUT_OF_STOCK -> HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.out_of_stock");
                case WRONG_MARKET, ITEM_NOT_FOUND ->
                        HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.item_not_found", itemId);
                case NOT_ENOUGH_MONEY -> HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.not_enough_money");
                case TASK_FAILED, TRANSACTION_ERROR, CANNOT_BUY_ITEM ->
                        HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.buy_failed");
                case PLAYER_OWNS_ITEM -> HMI18n.sendPlayerSync(player.getUniqueId(), "info.ui.market.player_owns_item");
            }
        });
    }

    @Override
    public CompletableFuture<List<ShopItemData>> getShopItems(UUID marketId) {
        return marketApi.getDatabaseManager().getAllShopItems(marketId).thenApplyAsync(shopItemDataList -> {
            List<ShopItemData> result = new ArrayList<>();
            shopItemDataList.ifPresent(result::addAll);
            result.removeIf((shopItemData) -> {
                if (shopItemData.amount() <= 0) {
                    try {
                        var removeOptional = marketApi.getDatabaseManager().removeShopItem(shopItemData.itemId()).get();//todo Merge SQL
                        if (removeOptional.isPresent() && removeOptional.get() > 0)
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

    public double getListingFee(@NotNull UUID marketId) {
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            return marketApi.getConfig().feeMarket;
        }
        return marketApi.getConfig().feeSignshop;
    }

    public double getTaxRate(@NotNull UUID marketId) {
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            return marketApi.getConfig().taxMarket / 100.0;
        }
        return marketApi.getConfig().taxSignshop / 100.0;
    }

    @Override
    public double getTaxRate(@NotNull ShopItemData shopItemData) {
        return getTaxRate(shopItemData.market());
    }

    public CompletableFuture<MarketItemDataResult> getShopItemData(int itemId) {
        return marketApi.getDatabaseManager().getShopItemData(itemId).thenApply(optShopItemData -> {
            if (optShopItemData.isEmpty()) {
                return MarketItemDataResult.fail(MarketItemDataResult.MarketItemDataStatus.NOT_FOUND);
            }
            return MarketItemDataResult.success(optShopItemData.get());
        });
    }


    @Contract(pure = true)
    private double getMarketStorageFeeRate(@NotNull UUID marketId) {
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            return (marketApi.getConfig().storageMarketPercent / 100.0d);
        } else {
            return (marketApi.getConfig().storageSignshopPercent / 100.0d);
        }
    }

    @Contract(pure = true)
    private double getMarketStorageFeeBase(@NotNull UUID marketId) {
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            return marketApi.getConfig().storageMarketBase;
        } else {
            return marketApi.getConfig().storageSignshopBase;
        }
    }

    @Contract(pure = true)
    private int getMarketStorageFreeDays(@NotNull UUID marketId) {
        if (marketId.equals(MarketIdUtils.getSystemShopId())) {
            return marketApi.getConfig().storageMarketFreedays;
        } else {
            return marketApi.getConfig().storageSignshopFreedays;
        }
    }

    @Override
    public void updateItem(long begin, long now) {
        if (begin >= now) return;
        marketApi.getDatabaseManager().getNeedUpdateItems(begin).thenAcceptAsync(shopItemDataList -> {
            if (shopItemDataList.isEmpty()) return;
            shopItemDataList.get().forEach(shopItemData -> {
                var marketId = shopItemData.market();
                var keepItems = true;
                keepItems = storageFee(TimeUtils.getUnixTimeStampNow(), getMarketStorageFreeDays(marketId), getMarketStorageFeeBase(marketId), getMarketStorageFeeRate(marketId), shopItemData);
                if (!keepItems) {
                    marketApi.getDatabaseManager().removeShopItem(shopItemData.itemId()).thenAccept(
                            (result) -> {
                                if (result.isEmpty() || result.get() <= 0) {
                                    HMLogUtils.logWarning("can't remove item: " + shopItemData.itemId() + " in shop " + shopItemData.market());
                                } else {
                                    HMLogUtils.logInfo("Item " + shopItemData.itemId() + " removed from shop " + shopItemData.market());
                                }

                            }
                    );
                    HMLogUtils.logInfo("Item " + shopItemData.itemId() + " removed from shop " + shopItemData.market());
                } else {
                    marketApi.getDatabaseManager().setItemUpdateTime(shopItemData.itemId(), now).thenAccept(
                            (result) -> {
                                if (result.isEmpty() || result.get() <= 0) {
                                    HMLogUtils.logWarning("can't update item " + shopItemData.itemId() + " in shop " + shopItemData.market());
                                }
                            }
                    );
                }
            });
        });
    }

    private boolean storageFee(long now, double freeDays, double base, double rate, ShopItemData shopItemData) {
        return TaskUtils.async.getSyncDefault(() -> {
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
                    var balance = marketApi.getEconomyCore().getPlayerBalance(shopItemData.owner());
                    if (marketApi.getEconomyCore().withdrawPlayer(shopItemData.owner(), fee)) {
                        marketApi.getEconomyCore().depositSystemVault(fee);
                        HMLogUtils.logInfo("[item update][" + TimeUtils.getUnixTimeStampNow() + "] " + shopItemData.owner() + " paid " + fee + " for storage fee. shop id:" + shopItemData.market());
                        return true;
                    }
                    HMLogUtils.logInfo("[item update][" + TimeUtils.getUnixTimeStampNow() + "] " + shopItemData.owner() + " not enough money to pay storage fee(" + fee + "). shop id:" + shopItemData.market());
                    return !(balance < fee);
                }
                , false);
    }
}
