package cat.nyaa.hmarket.api.implementations;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.api.IMarketShopLocation;
import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.db.data.ShopLocationData;
import cat.nyaa.hmarket.utils.*;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
                if (!value.getBlockLocationData().equals(key))
                    return CompletableFuture.completedFuture(false);
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
    public void onSignClick(@NotNull BlockLocationData blockLocationData, Player player, @NotNull PlayerInteractEvent event) {
        if (!cache.containsKey(blockLocationData)) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign))
            return;
        if (!sign.isWaxed()) {
            sign.setWaxed(true);
            sign.update();
        }
        var shopLocationData = cache.get(blockLocationData);
        event.setCancelled(true);
        var playerName = Bukkit.getOfflinePlayer(shopLocationData.market()).getName();
        Hmarket.getInstance().getViewServer().createViewForPlayer(player, shopLocationData.market(),
                HMI18n.format("info.ui.title.shop.user", playerName));
        Hmarket.getInstance().getViewServer().openViewForPlayer(player);
    }

    @Override
    public void onSignChange(@NotNull BlockLocationData fromLocation, @NotNull Player owner, @NotNull String[] lines, @NotNull Block block) {
        if (lines.length < 3) return;
        if (!lines[0].equalsIgnoreCase(SIGN_LINE0)) return;

        if (cache.containsKey(fromLocation)) {
            // it's normal in mc 1.20
            // HMLogUtils.logWarning("Shop already exists at " + fromLocation);
            // HMI18n.send(owner, "info.sign.occupied");
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
                                HMI18n.sendSync(ownerId, "info.sign.database_error");
                            } else if (result1.get() > 0) {
                                HMI18n.sendSync(ownerId, "info.sign.created");
                                cache.getAndUpdateCache(fromLocation);
                                Bukkit.getScheduler().runTask(Hmarket.getInstance(), () -> {
                                    var sign = ((Sign) block.getState());
                                    sign.setWaxed(true);
                                    sign.update();
                                });
                            } else {
                                HMI18n.sendSync(ownerId, "info.sign.create_failed");
                            }
                        }
                )
                .whenComplete(
                        (result1, throwable) -> {
                            if (throwable != null) {
                                HMI18n.sendSync(ownerId, "info.sign.database_error");
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
    public boolean isBlockProtected(@NotNull Block block, @Nullable Player player) {
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
        List<Block> nearby = Lists.newArrayList();
        final BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace direction : directions) {
            nearby.add(block.getRelative(direction));
        }
        nearby.removeIf(b ->
                {
                    if (b == null) return true;
                    return b.isEmpty();
                }
        );
        for (Block b : nearby) {
            if (b.getState() instanceof Sign signState) {
                if (signState.getBlockData() instanceof WallHangingSign) {
                    return false; // skip check for WallHangingSign
                }
                var baseBlock = getSignBaseBlock(signState);
                if (baseBlock.getLocation().equals(block.getLocation())) {
                    var signLocation = BlockLocationData.fromLocation(b.getLocation());
                    if (cache.containsKey(signLocation)) {
                        if (player != null) {
                            HMI18n.send(player, "info.sign.nearby-protected");
                        }
                        return true;
                    }
                }
            }
        }

        var fromLocation = BlockLocationData.fromLocation(block.getLocation());
        if (!cache.containsKey(fromLocation)) return false;
        if (player == null) {
            return true;
        }
        if (player.isOp()) return false;
        var shop = cache.get(fromLocation);
        var playerId = player.getUniqueId();
        if (!shop.owner().equals(playerId)) {
            HMI18n.send(player, "info.sign.not-owner");
            return true;
        }
        return false;

    }

    private Block getSignBaseBlock(Sign block) {
        BlockFace face;
        if (block.getBlockData() instanceof org.bukkit.block.data.type.WallSign wallSign) {
            face = wallSign.getFacing();
        } else if (block.getBlockData() instanceof org.bukkit.block.data.type.HangingSign hangingSign) {
            face = BlockFace.DOWN;
        } else if (block.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
            face = BlockFace.UP;
        } else {
//            @SuppressWarnings("deprecation") final org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) block.getData();
//            if (!signMat.isWallSign()) {
//                face = BlockFace.UP;
//            } else {
//                face = signMat.getFacing();
//            }
            throw new RuntimeException("Not detectable sign type.");
        }
        return block.getBlock().getRelative(face.getOppositeFace());
    }

    @Override
    public void onSignDestroy(@NotNull BlockLocationData fromLocation, @NotNull UUID playerId) {
        if (!cache.containsKey(fromLocation)) return;
        cache.remove(fromLocation).thenAccept(
                result -> {
                    if (result) {
                        HMI18n.sendSync(playerId, "info.sign.destroyed");
                        HMLogUtils.logInfo("Shop sign at " + fromLocation + " was destroyed by " + playerId);
                    } else {
                        HMI18n.sendSync(playerId, "info.sign.destroy_failed");
                        HMLogUtils.logWarning("Failed to destroy shop at " + fromLocation + " for player " + playerId);
                    }
                }
        );
    }

    @Override
    public Optional<ShopLocationData> getLocationData(BlockLocationData blockLocationData) {
        if (cache.containsKey(blockLocationData)) {
            return Optional.ofNullable(cache.get(blockLocationData));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public CompletableFuture<Optional<List<ShopLocationData>>> getLocationDataByOwner(UUID ownerId) {
        return marketApi.getDatabaseManager().getShopLocationByOwner(ownerId);
    }

    @Override
    public CompletableFuture<Optional<List<ShopLocationData>>> getLocationDataByMarket(UUID marketId) {
        return marketApi.getDatabaseManager().getShopLocationByMarket(marketId);
    }
}
