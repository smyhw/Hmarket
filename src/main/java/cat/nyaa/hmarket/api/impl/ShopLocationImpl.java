package cat.nyaa.hmarket.api.impl;

import cat.nyaa.aolib.database.SimpleKVCache;
import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.api.IMarketShopLocation;
import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.db.data.ShopLocationData;
import cat.nyaa.hmarket.utils.HMLogUtils;
import cat.nyaa.hmarket.utils.HMUiUtils;
import cat.nyaa.hmarket.utils.KeyUtils;
import cat.nyaa.hmarket.utils.TimeUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ShopLocationImpl implements IMarketShopLocation {
    private final static String SIGN_LINE0 = "[SHOP]";
    private final HMarketAPI marketApi;
    private final SimpleKVCache<BlockLocationData, ShopLocationData> cache;

    public ShopLocationImpl(HMarketAPI marketApi) {
        this.marketApi = marketApi;
        this.cache = new SimpleKVCache<>(new SimpleKVCache.simpleDataProvider<>() {
            @Override
            public CompletableFuture<Optional<ShopLocationData>> get(@NotNull BlockLocationData key) {
                return marketApi.getDatabaseManager().getShopLocationByPos(key.x(), key.y(), key.z(), key.world());
            }

            @Override
            public CompletableFuture<Optional<Map<BlockLocationData, ShopLocationData>>> getAll() {
                return marketApi.getDatabaseManager().getAllShopLocations().thenApply(shopLocationData -> {
                    var result = new HashMap<BlockLocationData, ShopLocationData>();
                    if (shopLocationData.isEmpty()) {
                        return Optional.empty();
                    }
                    if (shopLocationData.get().isEmpty()) {
                        return Optional.of(result);
                    }
                    for (ShopLocationData shopLocation : shopLocationData.get()) {
                        var key = new BlockLocationData(shopLocation.blockX(), shopLocation.blockY(), shopLocation.blockZ(), shopLocation.world());
                        if (result.containsKey(key)) {
                            HMLogUtils.logError("More than one shop location found at " + key);
                            return Optional.empty();
                        } else {
                            result.put(key, shopLocation);
                        }
                    }
                    return Optional.of(result);
                });
            }

            @Override
            public CompletableFuture<Boolean> insert(@NotNull BlockLocationData key, @NotNull ShopLocationData value) {
                if (!value.getBlockLocationData().equals(key)) return CompletableFuture.completedFuture(false);
                return marketApi.getDatabaseManager().insertShopLocation(value)
                        .thenApply(result -> {
                            if (result.isEmpty()) return false;
                            return result.get() > 0;
                        });
            }

            @Override
            public CompletableFuture<Boolean> update(@NotNull BlockLocationData key, @NotNull ShopLocationData value) {
                return marketApi.getDatabaseManager().updateShopLocation(key, value)
                        .thenApply(result -> {
                            if (result.isEmpty()) return false;
                            return result.get() > 0;
                        });
            }

            @Override
            public CompletableFuture<Boolean> remove(@NotNull BlockLocationData key) {
                return marketApi.getDatabaseManager()
                        .deleteShopLocationByPos(key.x(), key.y(), key.z(), key.world())
                        .thenApply(result -> {
                            if (result.isEmpty()) return false;
                            return result.get() > 0;
                        });
            }
        });
    }


    @Override
    public void onSignClick(@NotNull BlockLocationData blockLocationData, Player player, @NotNull Action action) {
        if (action != Action.RIGHT_CLICK_BLOCK) return;
        if (!cache.containsKey(blockLocationData)) return;
        var shopLocationData = cache.get(blockLocationData);
        HMUiUtils.openShopUi(player, shopLocationData.market());
    }

    @Override
    public void onSignChange(@NotNull BlockLocationData fromLocation, @NotNull Player owner, String @NotNull [] lines, @NotNull Block block) {
        if (lines.length < 3) return;
        if (!lines[0].equalsIgnoreCase(SIGN_LINE0)) return;

        if (cache.containsKey(fromLocation)) {
            HMLogUtils.logWarning("Shop already exists at " + fromLocation);
            HMI18n.send(owner, "info.sign.occupied");
            return;
        }

        block.getState().setMetadata(
                KeyUtils.SIGN_CREATE_LOCK,
                new FixedMetadataValue(marketApi.getDatabaseManager().getPlugin(), TimeUtils.getUnixTimeStampNow())
        );
        var ownerId = owner.getUniqueId();
        var limitSigns = marketApi.getConfig().limitSigns;

        marketApi.getDatabaseManager()
                .createShopLocation(fromLocation, ShopLocationData.ShopType.SIGN, ownerId, limitSigns)
                .thenAccept(
                        result1 -> {
                            if (result1.isEmpty()) {
                                HMI18n.sendPlayerSync(ownerId, "info.database_error");
                            } else if (result1.get() > 0) {
                                HMI18n.sendPlayerSync(ownerId, "info.sign.created");
                                cache.getAndUpdateCache(fromLocation);
                            } else {
                                HMI18n.sendPlayerSync(ownerId, "info.sign.create_failed");
                            }
                        }
                )
                .whenComplete(
                        (result1, throwable) -> {
                            if (throwable != null) {
                                HMI18n.sendPlayerSync(ownerId, "info.database_error");
                                HMLogUtils.logWarning("Failed to create shop at " + fromLocation);
                                throwable.printStackTrace();
                            }
                            TaskUtils.async.runSyncMethod(() -> {
                                if (block.getState().hasMetadata(KeyUtils.SIGN_CREATE_LOCK)) {
                                    block.getState().removeMetadata(KeyUtils.SIGN_CREATE_LOCK, marketApi.getDatabaseManager().getPlugin());
                                }
                            });
                        }
                );
    }

    @Override
    public boolean isBlockProtected(@NotNull Block block) {
        if (block.getState().hasMetadata(KeyUtils.SIGN_CREATE_LOCK)) {
            for (MetadataValue metadata : block.getState().getMetadata(KeyUtils.SIGN_CREATE_LOCK)) {
                if (metadata.getOwningPlugin() == null) continue;
                if (!metadata.getOwningPlugin().getName().equals(marketApi.getDatabaseManager().getPlugin().getName()))
                    continue;
                var time = metadata.asLong();
                if (time <= 0) continue;
                var passed = TimeUtils.getUnixTimeStampNow() - time;
                if (passed <= marketApi.getConfig().maxSignCreateLockTime && passed >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSignDestroy(@NotNull BlockLocationData fromLocation, @NotNull UUID playerId) {
        if (!cache.containsKey(fromLocation)) return;
        cache.remove(fromLocation).thenAccept(
                result -> {
                    if (result) {
                        HMI18n.sendPlayerSync(playerId, "info.sign.destroyed");
                        HMLogUtils.logInfo("Shop sign at " + fromLocation + " was destroyed by " + playerId);
                    } else {
                        HMI18n.sendPlayerSync(playerId, "info.sign.destroy_failed");
                        HMLogUtils.logWarning("Failed to destroy shop at " + fromLocation + " for player " + playerId);
                    }
                }
        );
    }

    @Override
    public Optional<ShopLocationData> getLocationData(BlockLocationData blockLocationData) {
        if (cache.containsKey(blockLocationData)) {
            return Optional.ofNullable(cache.get(blockLocationData));
        }else{
            return Optional.empty();
        }
    }
}
