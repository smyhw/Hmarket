package cat.nyaa.hmarket.gui;

import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.database.models.StoreModel;
import cat.nyaa.hmarket.database.tables.StoreTable;
import cat.nyaa.hmarket.ecore.EcoreManager;
import cat.nyaa.hmarket.shopitem.ItemCache;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BuySignShopUI extends ShopUI {

    private final UUID owner;

    public BuySignShopUI(UUID owner, List<ItemCache> goods) {
        super(goods);
        this.owner = owner;
    }

    protected void tradeBuy(Player player, @NotNull ItemCache target, int num) {
        var totalCost = target.price * num;
        if (!InventoryUtils.hasItem(player, target.nbt, num)) {
            new Message(I18n.format("shop.noEnoughItem")).append(target.nbt).send(player);
            return;
        }
        var provider = EcoreManager.getInstance().getEconomyCoreProvider();
        if (provider.getPlayerBalance(owner) < totalCost) {
            I18n.send(player.getPlayer(), "shop.insufficientBalance");
            return;
        }
        if (!provider.playerTrade(owner, player.getUniqueId(), totalCost).isSuccess()) {
            I18n.send(player.getPlayer(), "unknownerror");
            return;
        }
        InventoryUtils.removeItem(player, target.nbt, num);
        new Message("").append(I18n.format("shop.transaction.buy", num, player.getName(), totalCost), target.nbt).send(player);
        try {
            StoreTable.updateItem(new StoreModel(ItemStackUtils.itemToBase64(target.nbt), num, owner.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (check(slotNum, clickType, player)) return;
        var target = goods.get(slotNum);
        int num = getNum(target, clickType);
        tradeBuy(player, target, num);
    }




}
