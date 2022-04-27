package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.db.data.ShopLocationData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface IMarketShopLocation {
    void onSignClick(@NotNull BlockLocationData blockLocationData, Player player, @NotNull Action action);

    void onSignChange(@NotNull BlockLocationData fromLocation, @NotNull Player owner, @NotNull String[] lines, @NotNull Block block);
     boolean isBlockProtected(@NotNull Block block);

    void onSignDestroy(@NotNull BlockLocationData fromLocation,@NotNull UUID playerId);

    Optional<ShopLocationData> getLocationData(BlockLocationData blockLocationData);
}
