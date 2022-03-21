package cat.nyaa.hmarket;

import cat.nyaa.aolib.aoui.UIManager;
import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.command.CommandManager;
import cat.nyaa.hmarket.config.HMConfig;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import cat.nyaa.hmarket.task.HMTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class Hmarket extends JavaPlugin {

    @Nullable
    public static UIManager uiManager;
    private static Hmarket instance;
    @Nullable
    public EconomyCore economyProvider;
    @Nullable
    private HmarketDatabaseManager databaseManager;
    private HMConfig hmConfig;
    private HMI18n i18n;
    private CommandManager commandManager;
    private HMTaskManager taskManager;

    public static Hmarket getInstance() {
        return instance;
    }

    @Contract(pure = true)
    public static @Nullable HMarketAPI getAPI() {
        return instance == null ? null : instance.getHMarketAPI();
    }

    public @Nullable HMarketAPI getHMarketAPI() {
        if (databaseManager == null || economyProvider == null || hmConfig == null) return null;
        return new HMarketAPI(databaseManager, economyProvider, hmConfig);
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        if (!setupEconomy()) {
            this.getLogger().severe("ECore is not installed!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        uiManager = new UIManager(this);
        this.hmConfig = new HMConfig(this);
        this.i18n = new HMI18n(this, hmConfig.language);
        this.commandManager = new CommandManager(this, i18n);
        databaseManager = new HmarketDatabaseManager(this);
        this.taskManager = new HMTaskManager(this);
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
        if (databaseManager != null) {
            databaseManager.close();
            databaseManager = null;
        }
        if (uiManager != null) {
            uiManager.destructor();
            uiManager = null;
        }
        if (taskManager != null) {
            taskManager.destructor();
            taskManager = null;
        }
        instance = null;
    }
}
