package cat.nyaa.hmarket;

import cat.nyaa.aolib.aoui.UIManager;
import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.hmarket.command.CommandHandle;
import cat.nyaa.hmarket.database.DataSourceManager;
import cat.nyaa.hmarket.ecore.EcoreManager;
import cat.nyaa.hmarket.listener.ListenerManager;
import cat.nyaa.hmarket.schedule.ScheduleManager;
import cat.nyaa.hmarket.shopitem.ItemCacheManager;
import cat.nyaa.hmarket.shopitem.ShopItemManager;
import cat.nyaa.hmarket.signshop.SignShopManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class Hmarket extends JavaPlugin {

    private HmarketConfig conf;
    private CommandHandle commandHandle;
    private I18n i18n;
    @Nullable
    private static UIManager uiManager;
    private PluginCommand mainCommand;
    private ListenerManager listenerManager;
    private ScheduleManager scheduleManager;

    @Nullable
    public static UIManager getUiManager() {
        return uiManager;
    }


    @Override
    public void onEnable() {
        // Plugin startup logic
        if (this.getServer().getPluginManager().getPlugin("ECore") == null) {
            this.getLogger().severe("ECore is not installed!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.conf = new HmarketConfig(this);
        uiManager = new UIManager(this);
        DataSourceManager.init(conf);
        this.scheduleManager = ScheduleManager.Companion.getInstance();
        scheduleManager.init(this);
        this.i18n = new I18n(this, conf.language);
        this.commandHandle = new CommandHandle(this, i18n);
        this.mainCommand = getCommand("hmarket");
        this.listenerManager = new ListenerManager(this);
        if (mainCommand != null) {
            mainCommand.setExecutor(commandHandle);
            mainCommand.setTabCompleter(commandHandle);
        } else throw new RuntimeException("cannot load CommandHandle");
        var signShopManager = SignShopManager.getInstance();
        signShopManager.loadSignShopFromDb();
        var itemCacheManager = ItemCacheManager.getInstance();
        var ecoreManager = EcoreManager.getInstance();
        itemCacheManager.init(this);
        ecoreManager.init(Bukkit.getServicesManager().getRegistration(EconomyCore.class).getProvider());
        ShopItemManager.init(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (uiManager != null) {
            uiManager.destructor();
            uiManager = null;
        }
        if (DataSourceManager.getInstance() != null) {
            DataSourceManager.getInstance().close();
        }
    }
}
