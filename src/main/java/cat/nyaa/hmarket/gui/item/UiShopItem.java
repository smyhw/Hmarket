package cat.nyaa.hmarket.gui.item;

import cat.nyaa.aolib.network.data.DataClickType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record UiShopItem(ItemStack nbt) implements IClickableUiItem {
    @Override
    public ItemStack getWindowItem(Player player) {
        return nbt;
    }

    @Override
    public void onClick(DataClickType clickType, Player player) {

    }
}
