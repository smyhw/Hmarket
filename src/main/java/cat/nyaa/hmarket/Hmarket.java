package cat.nyaa.hmarket;

import cat.nyaa.aolib.aoui.UIManager;
import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.command.HMCommandHeader;
import cat.nyaa.hmarket.config.HMConfig;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Hmarket extends JavaPlugin {

    @Nullable
    public EconomyCore economyProvider;
    @Nullable
    private HmarketDatabaseManager databaseManager;
    private HMConfig hmConfig;
    private HMI18n i18n;
    private HMCommandHeader commandHandler;
    private static Hmarket instance;
    @Nullable
    public static UIManager uiManager;

    public static Hmarket getInstance() {
        return instance;
    }

    @Contract(pure = true)
    public static @Nullable HMarketAPI getAPI() {
        return instance == null ? null : instance.getHMarketAPI();
    }

    public @Nullable HMarketAPI getHMarketAPI() {
        if (databaseManager == null || economyProvider == null) return null;
        return new HMarketAPI(databaseManager, economyProvider);
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
        this.commandHandler = new HMCommandHeader(this, this.i18n);
        regCommand(this, "hmarket", commandHandler);
        databaseManager = new HmarketDatabaseManager(this);

    }

    private void regCommand(@NotNull Hmarket plugin, @NotNull String commandName, @NotNull CommandReceiver commandReceiver) {
        var pluginCommand = plugin.getCommand(commandName);
        if (pluginCommand == null) {
            plugin.getLogger().warning("Command registration failed : " + commandName + " not found.");
            return;
        }
        pluginCommand.setExecutor(commandReceiver);
        pluginCommand.setTabCompleter(commandReceiver);
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
        if(uiManager != null){
            uiManager.destructor();
            uiManager = null;
        }
        instance = null;
    }
}
