package cat.nyaa.hmarket.config;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class HMConfig extends PluginConfigure {
    private final Hmarket plugin;

    @Serializable
    public
    String language = LanguageRepository.DEFAULT_LANGUAGE;

    public HMConfig(Hmarket plugin) {
        this.plugin = plugin;
        load();
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }
}
