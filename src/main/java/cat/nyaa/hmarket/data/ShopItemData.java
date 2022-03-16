package cat.nyaa.hmarket.data;


import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public record ShopItemData(
        int itemId,
        String itemNbt,
        int amount,
        String owner, //owner id
        String market, //market uuid
        double price,
        long createdAt, //time
        long updatedAt, //time
        @Nullable String description
) {

    public static ShopItemData fromResultSet(ResultSet rs) throws SQLException {
        return new ShopItemData(
                rs.getInt("itemId"),
                rs.getString("itemNbt"),
                rs.getInt("amount"),
                rs.getString("owner"),
                rs.getString("market"),
                rs.getDouble("price"),
                rs.getLong("createdAt"),
                rs.getLong("updatedAt"),
                rs.getObject("description") == null ? null : (String) rs.getObject("description")
        );
    }
}
