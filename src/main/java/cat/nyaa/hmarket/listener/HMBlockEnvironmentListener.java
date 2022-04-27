package cat.nyaa.hmarket.listener;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.IMarketShopLocation;
import cat.nyaa.hmarket.utils.HMLogUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Optional;

public class HMBlockEnvironmentListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(HMBlockEnvironmentListener::isBlockProtected);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(HMBlockEnvironmentListener::isBlockProtected);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState blockstate : event.getBlocks()) {
            if (isBlockProtected(blockstate.getBlock())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isBlockProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isBlockProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isBlockProtected(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (isBlockProtected(block)) {
            event.setCancelled(true);
        }
    }

    private static Optional<IMarketShopLocation> getShopLocation() {
        return Optional.ofNullable(Hmarket.getAPI() != null ? Hmarket.getAPI().getShopLocationApi() : null);
    }

    private static boolean isBlockProtected(Block block) {
        return getShopLocation().map(shopLocation -> shopLocation.isBlockProtected(block)).orElseGet(() -> {
            HMLogUtils.logWarning("HMarket is not loaded, can not break block");
            return true;
        });
    }
}
