package cat.nyaa.hmarket.signshop;

import cat.nyaa.hmarket.database.LocationModel;
import cat.nyaa.hmarket.shopitem.ShopItemType;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SignShop {
    public Location location;
    private final UUID owner;
    private String[] lores;
    private SignShopType type;

    public SignShop(UUID owner, SignShopType type, String[] lores, Location location) {
        this.owner = owner;
        this.type = type;
        this.lores = lores;
        this.location = location;
    }

    public SignShop(LocationModel locationModel) {
        this.owner = locationModel.owner;
        this.location = new Location(Bukkit.getServer().getWorld(locationModel.world),
                locationModel.x, locationModel.y, locationModel.z);
        var gson = new Gson();
        this.lores = gson.fromJson(locationModel.lores, String[].class);
        this.type = SignShopType.valueOf(locationModel.type);
    }


    public void applySign() {
        var userName = Bukkit.getOfflinePlayer(owner).getName();
        assert userName != null;
        Block block = location.getBlock();
        Sign sign = (Sign) (block.getState());
        sign.setLine(0, getTitle());
        sign.setLine(1, userName);
        List<String> lore = List.of(lores);
        int msgSize = lore.size();
        for (int i = 0; i < 2; i++) {
            String line = i >= msgSize ? "" : lore.get(i);
            if (line == null || line.trim().equals("")) {
                continue;
            }
            sign.setLine(i + 2, line);
        }
        sign.update();
    }

    public UUID getOwner() {
        return this.owner;
    }

    public SignShopType getType() {
        return this.type;
    }

    public ShopItemType getShopItemType() {
        switch (this.type) {
            case BUY -> {
                return ShopItemType.SIGNSHOP_BUY;
            }
            case SELL -> {
                return ShopItemType.SIGNSHOP_SELL;
            }
            default -> {
                return null;
            }
        }
    }

    private String getTitle() {
        //TODO
        return "Buy";
    }

    public LocationModel toModel() {
        var world = this.location.getWorld().getName();
        var x = this.location.getX();
        var y = this.location.getY();
        var z = this.location.getZ();
        var owner = this.owner;
        var gson = new Gson();
        var lores = gson.toJson(this.lores);
        var type = this.type.name();
        return new LocationModel(world, x, y, z, owner, lores, type);
    }
}
