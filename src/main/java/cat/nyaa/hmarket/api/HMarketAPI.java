package cat.nyaa.hmarket.api;

import cat.nyaa.hmarket.api.implementations.MarketImpl;
import cat.nyaa.hmarket.api.implementations.ShopLocationImpl;
import cat.nyaa.hmarket.config.HMConfig;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import net.milkbowl.vault.economy.Economy;

public class HMarketAPI {

    private final HmarketDatabaseManager databaseManager;
    private final Economy economyCore;
    private final HMConfig config;
    private final MarketImpl marketAPI;
    private final IMarketShopLocation shopLocationApi;

    public HMarketAPI(HmarketDatabaseManager databaseManager, Economy economyCore, HMConfig config) {
        this.databaseManager = databaseManager;
        this.economyCore = economyCore;
        this.config = config;
        this.marketAPI = new MarketImpl(this);
        this.shopLocationApi = new ShopLocationImpl(this);
    }

    public Economy getEconomyCore() {
        return economyCore;
    }

    public HmarketDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public HMConfig getConfig() {
        return config;
    }

    public IMarketAPI getMarketAPI() {
        return marketAPI;
    }

    public IMarketShopLocation getShopLocationApi() {

        return shopLocationApi;
    }
}

