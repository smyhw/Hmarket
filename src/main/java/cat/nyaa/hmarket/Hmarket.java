package cat.nyaa.hmarket;

import cat.nyaa.ecore.EconomyCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class Hmarket extends JavaPlugin {

    @Nullable
    public static EconomyCore economyProvider;

    @Override
    public void onEnable() {
        // Plugin startup logic

        if (!setupEconomy()) {
            this.getLogger().severe("ECore is not installed!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

    }

    private boolean setupEconomy() {
        var rsp = Bukkit.getServicesManager().getRegistration(EconomyCore.class);
        if (rsp != null) {
            economyProvider = rsp.getProvider();
        }
        return economyProvider != null;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
