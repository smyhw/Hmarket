package cat.nyaa.hmarket;

import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.command.CommandManager;
import cat.nyaa.hmarket.config.HMConfig;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import cat.nyaa.hmarket.listener.HMListenerManager;
import cat.nyaa.hmarket.message.AoMessage;
import cat.nyaa.hmarket.task.HMTaskManager;
import cat.nyaa.hmarket.ui.HMarketViewServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public final class Hmarket extends JavaPlugin {

    private static Hmarket instance;
    @Nullable
    public Economy economyProvider = null;
    @Nullable
    private HmarketDatabaseManager databaseManager;
    private HMConfig hmConfig;
    private HMI18n i18n;
    private CommandManager commandManager;
    private HMTaskManager taskManager;
    private HMListenerManager listenerManager;
    private HMarketAPI api;
    private HMarketViewServer viewServer;
    private AoMessage aoMessage;

    public static Hmarket getInstance() {
        return instance;
    }

    @Contract(pure = true)
    public static @Nullable HMarketAPI getAPI() {
        return instance == null ? null : instance.getHMarketAPI();
    }

    public @Nullable HMarketAPI getHMarketAPI() {
        return api;
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
        this.hmConfig = new HMConfig(this);
        this.i18n = new HMI18n(this, hmConfig.language);
        this.commandManager = new CommandManager(this, i18n);
        databaseManager = new HmarketDatabaseManager(this);
        this.taskManager = new HMTaskManager(this);
        this.viewServer = new HMarketViewServer(this);
        this.aoMessage = new AoMessage(this);
        this.listenerManager = new HMListenerManager(this);
        this.api = new HMarketAPI(databaseManager, economyProvider, hmConfig);
    }

    public AoMessage getAoMessage() {
        return aoMessage;
    }

    public HMarketViewServer getViewServer() {
        return viewServer;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            System.out.println("找不到Vault！");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            System.out.println("挂载Vault失败！");
            return false;
        }
        economyProvider = rsp.getProvider();
        return economyProvider != null;
    }


    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
            databaseManager = null;
        }
        if (taskManager != null) {
            taskManager.destructor();
            taskManager = null;
        }
        if (listenerManager != null) {
            listenerManager.destructor();
            listenerManager = null;
        }
        if(aoMessage != null){
            aoMessage.destructor();
            aoMessage = null;
        }
        if(viewServer != null){
            viewServer.destrutor();
            viewServer = null;
        }
        instance = null;
    }

    public void onReload() {
        onDisable();
        onLoad();
        onEnable();
    }
}
