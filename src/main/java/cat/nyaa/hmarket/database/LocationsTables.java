package cat.nyaa.hmarket.database;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class LocationsTables {

    @Nullable
    public static LocationModel[] getLocationsByOwner(UUID owner) throws SQLException {
        return getLocations("UUID", owner.toString());
    }

    public static LocationModel[] getLocations() throws SQLException {
        return getLocations(null, null);
    }

    public static LocationModel[] getLocations(String where, String target) throws SQLException {
        var locations = new ArrayList<LocationModel>();
        var sql = "SELECT * FROM locations";
        if (where != null) {
            sql += " WHERE " + where + " = ?";
        }

        if (DataSourceManager.getInstance() == null) {
            return null;
        }
        try (var conn = DataSourceManager.getInstance().getConnection()) {
            try (var ps = conn.prepareStatement(sql)) {
                if (where != null) {
                    ps.setObject(1, target);
                }
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        var world = rs.getString(1);
                        var x = rs.getDouble(2);
                        var y = rs.getDouble(3);
                        var z = rs.getDouble(4);
                        var owner = UUID.fromString(rs.getString(5));
                        var lores = rs.getString(7);
                        var type = rs.getString(8);
                        locations.add(new LocationModel(world, x, y, z, owner, lores, type));
                    }
                    return Iterables.toArray(locations, LocationModel.class);
                }
            }
        }
    }

    public static void insertLocation(LocationModel locationModel) throws SQLException {
        if (DataSourceManager.getInstance() == null) {
            return;
        }
        try (var conn = DataSourceManager.getInstance().getConnection()) {
            try (var ps = conn.prepareStatement("INSERT INTO locations (world, x, y, z, owner, lores, type) VALUES (?,?,?,?,?,?,?)")) {
                ps.setObject(1, locationModel.world);
                ps.setObject(2, locationModel.x);
                ps.setObject(3, locationModel.y);
                ps.setObject(4, locationModel.z);
                ps.setObject(5, locationModel.owner);
                ps.setObject(6, locationModel.lores);
                ps.setObject(7, locationModel.type);
                ps.executeUpdate();
            }
        }
    }
}
