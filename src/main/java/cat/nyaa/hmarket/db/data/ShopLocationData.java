package cat.nyaa.hmarket.db.data;

import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.utils.MarketIdUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record ShopLocationData(

        int blockX,
        int blockY,
        int blockZ,
        String world,
        ShopType type,
        UUID owner,
        UUID market) {


    @Contract("_ -> new")
    public static @NotNull ShopLocationData fromResultSet(@NotNull ResultSet rs) throws SQLException {
        return new ShopLocationData(
                rs.getInt("blockX"),
                rs.getInt("blockY"),
                rs.getInt("blockZ"),
                rs.getString("world"),
                ShopType.valueOf(rs.getString("type")),
                MarketIdUtils.parseUUID(rs.getString("owner")),
                MarketIdUtils.parseMarketId(rs.getString("market"))
        );
    }

    @Contract(" -> new")
    public @NotNull BlockLocationData getBlockLocationData() {
        return new BlockLocationData(blockX, blockY, blockZ, world);
    }

    /*
    CREATE TABLE IF NOT EXISTS shop_location
(
    locationId  INTEGER
        PRIMARY KEY AUTOINCREMENT,
    type VARCHAR NOT NULL,
    blockX INTEGER NOT NULL,
    blockY INTEGER NOT NULL,
    blockZ INTEGER NOT NULL,
    world   VARCHAR NOT NULL,
    owner   VARCHAR NOT NULL,
    market  VARCHAR NOT NULL
);
     */
    public enum ShopType {
        SIGN, FRAME
    }
}
