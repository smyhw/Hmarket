package cat.nyaa.hmarket.database;

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
            try (var ps = conn.prepareStatement("INSERT INTO items (nbt, price, owner, type) VALUES (?,?,?,?)")) {
                ps.setObject(1, model.nbt);
                ps.setObject(2, model.price);
                ps.setObject(3, model.owner);
                ps.setObject(4, model.type);
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
                        var nbt = rs.getString(2);
                        var price = rs.getDouble(3);
                        var owner = UUID.fromString(rs.getString(4));
                        var type = rs.getString(5);
                        items.add(new ItemsModel(nbt, price, owner, type));
                    }
                    return items;
                }
            }
        }
    }
}
