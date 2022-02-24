package cat.nyaa.hmarket.database.tables;

import cat.nyaa.hmarket.database.DataSourceManager;
import cat.nyaa.hmarket.database.models.ItemsModel;
import cat.nyaa.hmarket.shopitem.ItemCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemTables {
    public static void insertItem(ItemsModel model) throws SQLException {

        if (DataSourceManager.getInstance() == null) {
            return;
        }
        try (var conn = DataSourceManager.getInstance().getConnection()) {
            try (var ps = conn.prepareStatement("INSERT INTO items (nbt, price, owner, type, uuid) VALUES (?,?,?,?,?)")) {
                ps.setObject(1, model.nbt);
                ps.setObject(2, model.price);
                ps.setObject(3, model.owner);
                ps.setObject(4, model.type);
                ps.setString(5, model.uuid.toString());
                ps.executeUpdate();
            }
        }
    }

    public static List<ItemsModel> selectAll() throws SQLException {
        var items = new ArrayList<ItemsModel>();

        if (DataSourceManager.getInstance() == null) {
            return null;
        }
        try (var conn = DataSourceManager.getInstance().getConnection()) {
            try (var ps = conn.prepareStatement("SELECT * FROM items ORDER BY owner")) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        var nbt = rs.getString(1);
                        var price = rs.getDouble(2);
                        var owner = UUID.fromString(rs.getString(3));
                        var type = rs.getString(4);
                        var uuid = UUID.fromString(rs.getString(5));
                        var item = new ItemsModel(nbt, price, owner, type, uuid);
                        items.add(item);
                    }
                    return items;
                }
            }
        }
    }

    public static void syncDirty(List<ItemCache> itemCaches) {
        if (DataSourceManager.getInstance() == null) {
            return;
        }
        try (var conn = DataSourceManager.getInstance().getConnection()) {
            try (var ps = conn.prepareStatement("UPDATE items SET nbt = ? WHERE uuid = ?")) {
                for (var item : itemCaches) {
                    ps.setObject(1, item.nbt);
                    ps.setObject(2, item.uuid.toString());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
