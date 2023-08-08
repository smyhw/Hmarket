package cat.nyaa.hmarket.listener;

import cat.nyaa.hmarket.Hmarket;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HMListenerManager {

    private final List<Listener> listerList = new ArrayList<>();

    public HMListenerManager(Hmarket plugin) {
        this.register(new HMSignShopListener(), plugin);
        this.register(new HMBlockEnvironmentListener(), plugin);
        this.register(plugin.getViewServer(), plugin);
    }

    public <T extends Listener> T register(T listener, @NotNull Hmarket plugin) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        this.listerList.add(listener);
        return listener;
    }

    public void destructor() {
        this.listerList.forEach(HandlerList::unregisterAll);
        this.listerList.clear();
    }

}
