package cat.nyaa.hmarket.api.data;

import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BlockLocationData(int x, int y, int z, @NotNull String world) {
    @Contract("_ -> new")
    public static @NotNull BlockLocationData fromLocation(@NotNull Location location) {
        if (!location.isWorldLoaded() || location.getWorld() == null) {
            throw new IllegalArgumentException("World is not loaded");
        }
       return new BlockLocationData(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockLocationData that = (BlockLocationData) o;
        return x == that.x && y == that.y && z == that.z && world.equals(that.world);
    }
}
