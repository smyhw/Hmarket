package cat.nyaa.hmarket.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class LocationModel {
    public String world;
    public double x;
    public double y;
    public double z;
    public UUID owner;
    public String lores;
    public String type;

    public LocationModel(String world, double x, double y, double z, UUID owner, String lores, String type) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.owner = owner;
        this.lores = lores;
        this.type = type;
    }

    public Location getLocation() {
        var world = Bukkit.getServer().getWorld(this.world);
        return new Location(world, x, y, z);
    }
}
