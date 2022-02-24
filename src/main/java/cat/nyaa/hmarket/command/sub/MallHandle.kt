package cat.nyaa.hmarket.command.sub

import cat.nyaa.hmarket.Hmarket
import cat.nyaa.hmarket.I18n
import cat.nyaa.hmarket.gui.MallShopUI
import cat.nyaa.hmarket.shopitem.ItemCacheManager
import cat.nyaa.hmarket.shopitem.ShopItem
import cat.nyaa.hmarket.shopitem.ShopItemManager
import cat.nyaa.hmarket.shopitem.ShopItemType
import cat.nyaa.nyaacore.ILocalizer
import cat.nyaa.nyaacore.Message
import cat.nyaa.nyaacore.cmdreceiver.Arguments
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver
import cat.nyaa.nyaacore.cmdreceiver.SubCommand
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class MallHandle(plugin: Plugin, _i18n: ILocalizer) : CommandReceiver(plugin, _i18n) {
    override fun getHelpPrefix(): String {
        return "hmall";
    }

    @SubCommand(value = "mall", permission = "hmarket.command.shop.mall")
    fun onMall(sender: CommandSender?, args: Arguments) {
        val player = asPlayer(sender)
        if (args.top() == null) {
            val ui = MallShopUI(ItemCacheManager.getInstance().mallItemCache);
            Hmarket.getUiManager()?.sendOpenWindow(player, ui)
            return;
        }
        val handItem = player.inventory.itemInMainHand.clone()
        if (handItem.type.isAir) {
            I18n.send(sender, "command.shop.isAir")
            return;
        }
        val price = args.nextDouble()
        val shopItem = ShopItem(player.uniqueId, price, handItem, ShopItemType.Mall)
        ShopItemManager.getInstance().addShopItem(shopItem)

        player.inventory.itemInMainHand.amount = 0;

        Message("").append(I18n.format("command.shop.buy.success", handItem.amount, price), handItem).send(player)
    }

}