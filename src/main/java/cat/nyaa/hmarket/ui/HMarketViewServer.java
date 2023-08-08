package cat.nyaa.hmarket.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class HMarketViewServer implements Listener {
    private final JavaPlugin pluginInstance;

    private final Map<UUID, HmarketShopView> viewMap = new HashMap<>();
    private final Set<UUID> interactedPlayers = new HashSet<>();

    private final BukkitTask resetTask;

    public HMarketViewServer(JavaPlugin pluginInstance) {
        this.pluginInstance = pluginInstance;
        //clear interactedPlayers set every tick to prevent clicks caused by accident
        resetTask = Bukkit.getScheduler().runTaskTimer(pluginInstance, interactedPlayers::clear, 0L, 1L);
    }

    public void openViewForPlayer(Player player) {
        player.openInventory(viewMap.get(player.getUniqueId()).getUi());
    }

    public void createViewForPlayer(Player player, UUID marketId, Component title) {
        viewMap.put(player.getUniqueId(), new HmarketShopView(player, marketId, title));
    }

    public void destrutor(){
        viewMap.values().forEach(t -> t.getUi().close());
        viewMap.clear();
        resetTask.cancel();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null ||
                !viewMap.containsKey(event.getWhoClicked().getUniqueId())) return;
        if (event.getClickedInventory() == viewMap.get(event.getWhoClicked().getUniqueId()).getUi()) {
            event.setCancelled(true);
            var item = event.getCurrentItem();
            if (item == null || item.getType().isAir()) return;
            if (interactedPlayers.contains(event.getWhoClicked().getUniqueId()))
                return; //can interact with ui only once per tick
            viewMap.get(event.getWhoClicked().getUniqueId()).onClick((Player) event.getWhoClicked(), event.getAction(), item, event.getSlot());
            interactedPlayers.add(event.getWhoClicked().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (viewMap.containsKey(event.getWhoClicked().getUniqueId())
                && event.getInventory() == viewMap.get(event.getWhoClicked().getUniqueId()).getUi())
            event.setCancelled(true);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (viewMap.containsKey(event.getPlayer().getUniqueId()))
            if (event.getInventory() == viewMap.get(event.getPlayer().getUniqueId()).getUi()) {
                viewMap.remove(event.getPlayer().getUniqueId());
            }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        viewMap.remove(event.getPlayer().getUniqueId());
    }

}
