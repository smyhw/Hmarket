package cat.nyaa.hmarket.gui;

import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.ecore.EcoreManager;
import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SellSignShopUI extends ShopUI {

    private final UUID owner;

    public SellSignShopUI(UUID owner, List<ItemCache> goods) {
        super(goods);
        this.owner = owner;
    }

    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (check(slotNum, clickType, player)) return;

        var target = goods.get(slotNum);
        int num = getNum(target, clickType);
        tradeSell(slotNum, player, target, num);

        var ownerPlayer = Objects.requireNonNullElse(Bukkit.getPlayer(this.owner), Bukkit.getOfflinePlayer(this.owner));
        new Message("").append(I18n.format("shop.transaction.sell", num, ownerPlayer.getName(), num * target.price), target.nbt).send(player);
    }

    @Nullable
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
        var transactionResult = provider.playerTrade(player.getUniqueId(), owner, totalCost);
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
