package cat.nyaa.hmarket.ecore;

import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.ecore.EconomyCoreProvider;
import org.bukkit.Bukkit;


public class EcoreManager {
    private EconomyCore provider;

    private static class SingletonHolder {
        private static final EcoreManager INSTANCE = new EcoreManager();
    }

    private EcoreManager() {
    }

    public void init(EconomyCore provider) {
        this.provider = provider;
    }

    public static final EcoreManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public EconomyCore getEconomyCoreProvider() {
        return provider;
    }
}
