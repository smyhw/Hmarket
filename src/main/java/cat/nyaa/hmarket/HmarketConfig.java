package cat.nyaa.hmarket;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class HmarketConfig extends PluginConfigure {
    private final Hmarket p;

    @Serializable
    public String DBFile = "Hmarket.db";

    @Serializable
    public String language = "en_US";

    public HmarketConfig(Hmarket p) {
        this.p = p;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.p;
    }
}
