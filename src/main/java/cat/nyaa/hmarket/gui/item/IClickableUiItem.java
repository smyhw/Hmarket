package cat.nyaa.hmarket.gui.item;

import cat.nyaa.aolib.aoui.item.IUiItem;
import cat.nyaa.aolib.network.data.DataClickType;
import org.bukkit.entity.Player;

public interface IClickableUiItem extends IUiItem {
    public void onClick(DataClickType clickType, Player player);
}
