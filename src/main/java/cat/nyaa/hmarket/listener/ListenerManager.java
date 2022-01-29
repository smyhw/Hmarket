package cat.nyaa.hmarket.listener;

import cat.nyaa.hmarket.Hmarket;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {
    private List<Listener> listeners = new ArrayList<>();

    public ListenerManager(Hmarket h) {
        this.listeners.add(new SignShopEventListener());

        for (Listener l : listeners) {
            h.getServer().getPluginManager().registerEvents(l, h);
        }
    }
}
