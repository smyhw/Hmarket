package cat.nyaa.hmarket.listener;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.gui.BuySignShopUI;
import cat.nyaa.hmarket.gui.SellSignShopUI;
import cat.nyaa.hmarket.gui.ShopUI;
import cat.nyaa.hmarket.shopitem.ItemCacheManager;
import cat.nyaa.hmarket.shopitem.ShopItemType;
import cat.nyaa.hmarket.signshop.SignShopManager;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class SignShopEventListener implements Listener {

    @EventHandler
    public void ClickSign(@NotNull PlayerInteractEvent e) {
        var clickedBlock = e.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)) return;
        if (!SignShopManager.getInstance().isSignShop(clickedBlock.getLocation())) return;
        var shop = SignShopManager.getInstance().getSignShopByLocation(clickedBlock.getLocation());
        var goods = ItemCacheManager.getInstance().getSignItemCache(shop.getOwner(), shop.getShopItemType());
        ShopUI shopUI;
        if (shop.getShopItemType() == ShopItemType.SIGNSHOP_BUY) {
            shopUI = new BuySignShopUI(shop.getOwner(), goods);
        } else if (shop.getShopItemType() == ShopItemType.SIGNSHOP_SELL) {
            shopUI = new SellSignShopUI(shop.getOwner(), goods);
        } else {
            return;
        }
        shopUI.applyGoods();
        Hmarket.getUiManager().sendOpenWindow(e.getPlayer(), shopUI);
    }

    @EventHandler
    public void SignShopBreak(BlockBreakEvent e) {
        var block = e.getBlock();
        if (!(block.getState() instanceof Sign)) {
            return;
        }
        if (!SignShopManager.getInstance().isSignShop(block.getLocation())) {
            return;
        }
        var sign = SignShopManager.getInstance().getSignShopByLocation(block.getLocation());
        SignShopManager.getInstance().removeSignShop(sign);
        I18n.send(e.getPlayer(), "shop.destroy");
    }
}
