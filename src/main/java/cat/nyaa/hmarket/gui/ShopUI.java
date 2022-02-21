package cat.nyaa.hmarket.gui;

import cat.nyaa.aolib.aoui.IBaseUI;
import cat.nyaa.aolib.aoui.item.EmptyUIItem;
import cat.nyaa.aolib.aoui.item.IUiItem;
import cat.nyaa.aolib.aoui.item.PlayerInventoryItem;
import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.gui.item.ButtonUIItem;
import cat.nyaa.hmarket.gui.item.UiShopItem;
import cat.nyaa.hmarket.shopitem.ItemCache;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopUI implements IBaseUI {
    private static final int WINDOW_ID = 112; // <127
    public static final int PAGE_SIZE = 45;
    private static final int PLAYER_INVENTORY = 53;
    protected int page = 1;
    List<IUiItem> uiItemList = new ArrayList<>();
    protected List<ItemCache> goods = null;

    public ShopUI(List<ItemCache> goods) {
        this.goods = goods;
        this.fillEmpty(9);
        this.addButton();
        addPlayerInventoryItem();
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public int getGoodsSize() {
        return goods.size();
    }

    public boolean hasNextPage() {
        return goods.size() > PAGE_SIZE * page;
    }

    public boolean hasPrevPage() {
        return page > 1;
    }

    public void addButton() {
        var buttonPrev = new ButtonUIItem(-1, this);
        var buttonNext = new ButtonUIItem(1, this);
        if (hasPrevPage()) {
            uiItemList.set(45, buttonPrev);
        }
        if (hasNextPage()) {
            uiItemList.set(53, buttonNext);
        }
    }

    protected boolean check(int slotNum, DataClickType clickType, Player player) {
        if (!clickType.equals(DataClickType.PICKUP) && !clickType.equals(DataClickType.PICKUP_ALL)) {
            return true;
        }
        // Player Inventory
        if (slotNum > PLAYER_INVENTORY) {
            return true;
        }
        if (uiItemList.get(slotNum) == EmptyUIItem.EMPTY_UI_ITEM) {
            return true;
        }
        if (uiItemList.get(slotNum) instanceof ButtonUIItem) {
            ((ButtonUIItem) uiItemList.get(slotNum)).onClick(clickType, player);
            return true;
        }
        return false;
    }

    public void fillEmpty(int cols) {
        for (int j = 0; j < 6; ++j) {
            for (int k = 0; k < cols; ++k) {
                uiItemList.add(EmptyUIItem.EMPTY_UI_ITEM);
            }
        }
    }

    public void applyGoods() {
        this.fillEmpty(8);
        for (int i = 0; i < PAGE_SIZE; i++) {
            int goodsIndex = i + (page - 1) * PAGE_SIZE;
            if (goodsIndex >= goods.size()) {
                break;
            }

            var item = goods.get(goodsIndex).nbt.clone();
            var prize = goods.get(goodsIndex).price;
            var meta = item.getItemMeta().clone();
            var lores = new ArrayList<String>();
            lores.add(I18n.format("shop.itemPrize", prize, 0.01));
            meta.setLore(lores);
            item.setItemMeta(meta);

            uiItemList.set(i, new UiShopItem(item));
        }
        if (Hmarket.getUiManager() == null) return;

        Hmarket.getUiManager().broadcastChanges(this);
    }


    protected void addPlayerInventoryItem() {
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                uiItemList.add(new PlayerInventoryItem(j1 + l * 9 + 9));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            uiItemList.add(new PlayerInventoryItem(i1));
        }
    }

    @Override
    public void onWindowClose() {

    }

    @Override
    public int getWindowId() {
        return WINDOW_ID;
    }

    @Override
    public int getTypeId() {
        return 5;
    }//GENERIC_9x6

    @Override
    public @NotNull BaseComponent getTitle() {
        return new TextComponent("test");
    }

    @Override
    public @NotNull List<ItemStack> getWindowItem(Player player) {
        return uiItemList.stream().map(uiItem -> uiItem.getWindowItem(player)).collect(Collectors.toList());
    }

    @Override
    public @NotNull ItemStack getCarriedWindowItem() {
        return new ItemStack(Material.AIR);
    }

    @Override
    public int getSlotSize() {
        return 6 * 9 + 3 * 9 + 9;//GENERIC_9x6
    }

    @Override
    public int[] getWindowData(Player player) {
        return new int[0];
    }

    @Override
    public void onButtonClick(int buttonId, Player player) {

    }

    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {

    }

    @Override
    public int getDataSize() {
        return 0;
    }
}
