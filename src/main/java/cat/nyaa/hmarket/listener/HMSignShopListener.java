package cat.nyaa.hmarket.listener;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.data.BlockLocationData;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class HMSignShopListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if(event.getPlayer().isSneaking()) return;
        var shopLocation = Hmarket.getAPI() != null ? Hmarket.getAPI().getShopLocationApi() : null;
        if (shopLocation == null) return;
        var clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        if (!(clickedBlock.getState() instanceof Sign signStale)) return;//no sign
        shopLocation.onSignClick(
                BlockLocationData.fromLocation(event.getClickedBlock().getLocation()),
                event.getPlayer(),
                event.getAction()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(@NotNull SignChangeEvent event) {
        var shopLocation = Hmarket.getAPI() != null ? Hmarket.getAPI().getShopLocationApi() : null;
        var plugin = Hmarket.getInstance();
        if (shopLocation == null || plugin == null) return;
        shopLocation.onSignChange(
                BlockLocationData.fromLocation(event.getBlock().getLocation()),
                event.getPlayer(),
                event.getLines(),
                event.getBlock()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        var shopLocation = Hmarket.getAPI() != null ? Hmarket.getAPI().getShopLocationApi() : null;
        if (shopLocation == null) return;
        var block = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        shopLocation.onSignDestroy(
                BlockLocationData.fromLocation(event.getBlock().getLocation()),
                event.getPlayer().getUniqueId()
        );
    }


}
