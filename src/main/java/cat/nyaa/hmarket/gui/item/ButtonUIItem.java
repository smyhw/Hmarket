package cat.nyaa.hmarket.gui.item;

import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.gui.ShopUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ButtonUIItem implements IClickableUiItem {
    private final int type;
    private final ShopUI shopUI;

    public ButtonUIItem(int type, ShopUI shopUI) {
        this.type = type;
        this.shopUI = shopUI;
    }


    @Override
    public void onClick(DataClickType clickType, Player player) {
        var newPage = this.shopUI.getPage() + type;
        if (newPage >= 1 && newPage <= this.shopUI.getGoodsSize() * ShopUI.PAGE_SIZE) {
            this.shopUI.setPage(newPage);
            this.shopUI.applyGoods();
        }
    }

    @Override
    public ItemStack getWindowItem(Player player) {
        var itemStack = new ItemStack(Material.ARROW);
        var meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.type == 1 ? I18n.format("shop.next") : I18n.format("shop.last"));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
