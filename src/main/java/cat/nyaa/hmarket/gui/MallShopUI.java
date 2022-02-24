package cat.nyaa.hmarket.gui;

import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.ecore.EcoreManager;
import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MallShopUI extends SellSignShopUI {
    public MallShopUI(List<ItemCache> goods) {
        super(null, goods);
    }

    @Override
    protected void tradeSell(int slotNum, @NotNull Player player, @NotNull ItemCache target, int num) {
        var provider = EcoreManager.getInstance().getEconomyCoreProvider();
        var totalCost = target.price * num;
        var goodIndex = PAGE_SIZE * (page - 1) + slotNum;
        var item = goods.get(goodIndex).nbt.clone();
        if (provider.getPlayerBalance(player.getUniqueId()) < totalCost) {
            I18n.send(player.getPlayer(), "shop.insufficientBalance");
            return;
        }
        if (!InventoryUtils.hasEnoughSpace(player, item, num)) {
            I18n.send(player.getPlayer(), "shop.noEnoughSpace");
            return;
        }
        var transactionResult = provider.playerTrade(player.getUniqueId(), target.owner, totalCost);
        if (!transactionResult.isSuccess()) {
            I18n.send(player.getPlayer(), "unknownerror");
            return;
        }
        var newAmount = item.getAmount() - num;
        item.setAmount(num);
        InventoryUtils.addItem(player.getInventory(), item);
        goods.get(goodIndex).nbt.setAmount(newAmount);
        refreshGui();
    }
}
