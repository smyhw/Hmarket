package cat.nyaa.hmarket.api;

import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.hmarket.api.impl.MarketImpl;
import cat.nyaa.hmarket.api.impl.ShopLocationImpl;
import cat.nyaa.hmarket.config.HMConfig;
import cat.nyaa.hmarket.db.HmarketDatabaseManager;
import cat.nyaa.hmarket.listener.HMSignShopListener;

public class HMarketAPI {

    private final HmarketDatabaseManager databaseManager;
    private final EconomyCore economyCore;
    private final HMConfig config;
    private final MarketImpl marketAPI;
    private final IMarketShopLocation shopLocationApi;

    public HMarketAPI(HmarketDatabaseManager databaseManager, EconomyCore economyCore, HMConfig config) {
        this.databaseManager = databaseManager;
        this.economyCore = economyCore;
        this.config = config;
        this.marketAPI = new MarketImpl(this);
        this.shopLocationApi = new ShopLocationImpl(this);
    }

    public EconomyCore getEconomyCore() {
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

