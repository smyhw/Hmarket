package cat.nyaa.hmarket.database;

import java.sql.SQLException;

public class StoreTable {

    public static void updateItem(StoreModel storeModel) throws SQLException {
        if (DataSourceManager.getInstance() == null) {
            return;
        }
        try (var conn = DataSourceManager.getInstance().getConnection()) {
            try (var ps = conn.prepareStatement("UPDATE store SET amount = amount + ? WHERE (owner = ? AND nbt = ?)")) {
                ps.setObject(1, storeModel.amount);
                ps.setObject(2, storeModel.owner);
                ps.setObject(3, storeModel.nbt);
                if (ps.executeUpdate() == 0) {
                    try (var ps1 = conn.prepareStatement("INSERT INTO store (nbt, amount, owner) VALUES (?,?,?)")) {
                        ps1.setObject(1, storeModel.nbt);
                        ps1.setObject(2, storeModel.amount);
                        ps1.setObject(3, storeModel.owner);
                        ps1.executeUpdate();
                    }
                }
            }
        }
    }
}
