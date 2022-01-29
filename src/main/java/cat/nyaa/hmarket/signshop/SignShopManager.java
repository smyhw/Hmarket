package cat.nyaa.hmarket.signshop;

import cat.nyaa.hmarket.database.LocationModel;
import cat.nyaa.hmarket.database.LocationsTables;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SignShopManager {

    private static final class InstanceHolder {
        private static final SignShopManager instance = new SignShopManager();
    }

    public static SignShopManager getInstance() {
        return SignShopManager.InstanceHolder.instance;
    }

    private Map<Location, SignShop> locationSignShopMap = new HashMap<>();

    public void loadSignShopFromDb() {
        try {
            var data = LocationsTables.getLocations();
            for (LocationModel l : data) {
                locationSignShopMap.put(l.getLocation(), new SignShop(l));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSignShop(SignShop shop) {
        this.locationSignShopMap.put(shop.location, shop);
        try {
            LocationsTables.insertLocation(shop.toModel());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isSignShop(Location location) {
        return locationSignShopMap.get(location) != null;
    }

    public SignShop getSignShopByLocation(Location location) {
        return locationSignShopMap.get(location);
    }
}
