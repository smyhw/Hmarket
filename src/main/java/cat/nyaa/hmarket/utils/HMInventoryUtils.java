package cat.nyaa.hmarket.utils;

import cat.nyaa.nyaacore.utils.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HMInventoryUtils {
    public static void giveOrDropItem(Player player, ItemStack itemStack) {
        if(InventoryUtils.hasEnoughSpace(player.getInventory(), itemStack)){
           if(InventoryUtils.addItem(player.getInventory(), itemStack)){
               return;
           }
        }
        player.getWorld().dropItem(player.getLocation(), itemStack);
    }
}
