package cat.nyaa.hmarket.db;

import cat.nyaa.aolib.utils.DBFunctionUtils;
import cat.nyaa.aolib.utils.DatabaseUtils;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.db.data.ShopItemData;
import cat.nyaa.hmarket.db.data.ShopLocationData;
import cat.nyaa.hmarket.utils.HMLogUtils;
import cat.nyaa.hmarket.utils.TimeUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class HmarketDatabaseManager {
    public static final String TABLE_SHOP_ITEM = "shop_item";
    public static final String TABLE_SHOP_LOCATION = "shop_location_v2";
    private static final LinkedBlockingQueue<Runnable> databaseExecutorQueue = new LinkedBlockingQueue<>();
    public static final ExecutorService databaseExecutor =
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, databaseExecutorQueue);
    private final Connection connection;
    private final Hmarket plugin;


    public Hmarket getPlugin() {
        return plugin;
    }

    public HmarketDatabaseManager(Hmarket plugin) {
        this.plugin = plugin;
        HMLogUtils.logInfo("Connecting to database...");
        Optional<Connection> optConn = Optional.empty();
        try {
            optConn = DatabaseUtils.newSqliteJdbcConnection(plugin).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (optConn.isEmpty()) {
            throw new RuntimeException("Failed to connect to database");
        }
        this.connection = optConn.get();
        try {
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initDatabase(plugin);
    }

    @Contract("_ -> new")
    public static <U> @NotNull CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        if (databaseExecutor.isShutdown()) throw new RuntimeException("playerDataExecutor is shutdown");
        return CompletableFuture.supplyAsync(supplier, databaseExecutor);
    }

    private void initDatabase(@NotNull Hmarket plugin) {
        HMLogUtils.logInfo("Initializing database...");
        var res = plugin.getResource("sql/" + "init.sql");
        if (res == null) {
            throw new RuntimeException("Failed to load init.sql");
        }
        byte[] sqlBytes;
        try {
            sqlBytes = res.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> sqlList = splitSql(sqlBytes);
        try (var statement = connection.createStatement()) {
            for (String sql : sqlList) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private @NotNull List<String> splitSql(byte @NotNull [] sqlBytes) {
        List<String> result = Lists.newArrayList();
        for (int i = 0, j = 0; i < sqlBytes.length; i++) {
            if (sqlBytes[i] == ';' || i == sqlBytes.length - 1) {
                var sql = new String(sqlBytes, j, i - j + 1);
                j = i + 1;
                if (sql.isEmpty() || sql.equals(";")) continue;
                result.add(sql);
            }
        }
        return result;
    }

    public void close() {
        try {
            this.connection.close();
        } catch (SQLException ignored) {
        }
    }

    public CompletableFuture<Optional<ShopItemData>> getShopItemData(int itemId) {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getShopItemById.sql",
                databaseExecutor, DBFunctionUtils.getDataFromResultSet(ShopItemData.class), itemId);
    }

    public CompletableFuture<Optional<Boolean>> removeItemsFromShop(int itemId, int amount) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "UpdateShopItemAmount.sql",
                        databaseExecutor, amount, itemId, amount)
                .thenApply((i) -> i.map(j -> (j) > 0));
    }

    public CompletableFuture<Optional<Integer>> addItemToShop(ItemStack items, int amount, @NotNull UUID ownerId,
                                                              @NotNull UUID marketId, double price, int limit) {
        return DatabaseUtils.executeUpdateAsyncAndGetAutoGeneratedKeys(
                connection, plugin, "addItemToShop.sql",
                databaseExecutor,DBFunctionUtils.getAutoGeneratedKey(),
                ItemStackUtils.itemToBase64(items), amount, ownerId.toString(), marketId.toString(), price,
                TimeUtils.getUnixTimeStampNow(), TimeUtils.getUnixTimeStampNow(),
                marketId.toString(), ownerId.toString()
                , limit
        );
    }

    public CompletableFuture<Optional<Integer>> insertItemsToShop(ItemStack items, int amount, @NotNull UUID ownerId,
                                                                  @NotNull UUID marketId, double price) {
        return DatabaseUtils.executeUpdateAsyncAndGetAutoGeneratedKeys(connection, plugin, "insertShopItem.sql",
                databaseExecutor, DBFunctionUtils.getAutoGeneratedKey(),
                ItemStackUtils.itemToBase64(items), amount, ownerId.toString(), marketId.toString(), price,
                TimeUtils.getUnixTimeStampNow(), TimeUtils.getUnixTimeStampNow());
    }

    public CompletableFuture<Optional<List<ShopItemData>>> getAllShopItems(@NotNull UUID marketId) {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getAllShopItems.sql",
                databaseExecutor, DBFunctionUtils.getDataListFromResultSet(ShopItemData.class), marketId.toString());
    }

    public @NotNull CompletableFuture<Optional<Integer>> removeShopItem(int itemId) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "removeShopItemById.sql",
                databaseExecutor, itemId);
    }

    public @NotNull CompletableFuture<Optional<List<ShopItemData>>> getNeedUpdateItems(long begin) {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getNeedUpdateItems.sql",
                databaseExecutor, DBFunctionUtils.getDataListFromResultSet(ShopItemData.class), begin);
    }

    public @NotNull CompletableFuture<Optional<Integer>> setItemUpdateTime(int itemId, long now) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "setShopItemUpdateTime.sql",
                databaseExecutor, now, itemId);
    }

    public CompletableFuture<Optional<Integer>> getShopAllItemCount(@NotNull UUID marketId) {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getAllShopItems.sql",
                databaseExecutor, DBFunctionUtils.getFirstData(Integer.class), marketId.toString());
    }

    public CompletableFuture<Optional<Integer>> getShopItemCountByOwner(@NotNull UUID marketId, @NotNull UUID ownerId) {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getShopItemCountByOwner.sql",
                databaseExecutor, DBFunctionUtils.getFirstData(Integer.class), marketId.toString(), ownerId.toString());
    }

    public CompletableFuture<Optional<Boolean>> buyItemFromMarket(@NotNull UUID market, int itemId, int amount, double price) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "buyItemFromMarket.sql",
                        databaseExecutor,
                        amount, itemId, market.toString(), price, amount)
                .thenApply((i) -> i.map(j -> (j) > 0));
    }

    public CompletableFuture<Optional<Integer>> insertShopLocation(@NotNull ShopLocationData shopLocationData) {
        return insertShopLocation(shopLocationData.type(), shopLocationData.blockX(), shopLocationData.blockY(),
                shopLocationData.blockZ(), shopLocationData.world(), shopLocationData.owner(), shopLocationData.market());
    }

    public CompletableFuture<Optional<Integer>> insertShopLocation(
            @NotNull ShopLocationData.ShopType type, int blockX, int blockY, int blockZ,
            @NotNull String world, @NotNull UUID owner, @NotNull UUID market) {
        return DatabaseUtils.executeUpdateAsyncAndGetAutoGeneratedKeys(connection, plugin, "insertShopLocation.sql",
                databaseExecutor, DBFunctionUtils.getAutoGeneratedKey(),
                blockX, blockY, blockZ, world, type, owner, market);
    }

    public @NotNull CompletableFuture<Optional<ShopLocationData>> getShopLocationByPos(
            int blockX, int blockY, int blockZ, @NotNull String world) {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getShopLocationByPos.sql",
                databaseExecutor, DBFunctionUtils.getDataFromResultSet(ShopLocationData.class),
                blockX, blockY, blockZ, world);
    }


    public CompletableFuture<Optional<Integer>> deleteShopLocationByPos(
            int blockX, int blockY, int blockZ, @NotNull String world) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "deleteShopLocationByPos.sql", databaseExecutor,
                blockX, blockY, blockZ, world);
    }

    public @NotNull CompletableFuture<Optional<@NotNull List<ShopLocationData>>> getAllShopLocations() {
        return DatabaseUtils.executeQueryAsync(connection, plugin, "getAllShopLocations.sql", databaseExecutor,
                DBFunctionUtils.getDataListFromResultSet(ShopLocationData.class));
    }

    public CompletableFuture<Optional<Integer>> updateShopLocation(
            @NotNull BlockLocationData key, @NotNull ShopLocationData value) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "updateShopLocation.sql", databaseExecutor,
                value.blockX(), value.blockY(), value.blockZ(), value.world(), value.type(), value.owner(), value.market(),
                key.x(), key.y(), key.z(), key.world());
    }

    public CompletableFuture<Optional<Integer>> createShopLocation(
            @NotNull BlockLocationData key, ShopLocationData.ShopType type, UUID player, int limit) {
        return createShopLocation(key.x(), key.y(), key.z(), key.world(), type, player, player, limit);
    }

    public CompletableFuture<Optional<Integer>> createShopLocation(
            int blockX, int blockY, int blockZ, String world, ShopLocationData.ShopType type,
            UUID owner, UUID market, int limit) {
        return DatabaseUtils.executeUpdateAsync(connection, plugin, "createShopLocation.sql", databaseExecutor,
                blockX, blockY, blockZ, world, type, owner, market, owner, limit);
    }


}