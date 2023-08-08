package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.db.data.ShopLocationData;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMarketShopLocation {
    void onSignClick(@NotNull BlockLocationData blockLocationData, Player player, @NotNull PlayerInteractEvent event);

    void onSignChange(@NotNull BlockLocationData fromLocation, @NotNull Player owner, @NotNull List<Component> lines, @NotNull Block block);

    boolean isBlockProtected(@NotNull Block block, @Nullable Player player);

    void onSignDestroy(@NotNull BlockLocationData fromLocation, @NotNull UUID playerId);

    Optional<ShopLocationData> getLocationData(BlockLocationData blockLocationData);

    CompletableFuture<Optional<List<ShopLocationData>>> getLocationDataByOwner(UUID ownerId);

    CompletableFuture<Optional<List<ShopLocationData>>> getLocationDataByMarket(UUID marketId);
}
