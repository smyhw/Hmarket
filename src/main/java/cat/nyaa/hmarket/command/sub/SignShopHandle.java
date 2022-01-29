package cat.nyaa.hmarket.command.sub;

import cat.nyaa.hmarket.I18n;
import cat.nyaa.hmarket.shopitem.ShopItem;
import cat.nyaa.hmarket.shopitem.ShopItemManager;
import cat.nyaa.hmarket.shopitem.ShopItemType;
import cat.nyaa.hmarket.signshop.SignShop;
import cat.nyaa.hmarket.signshop.SignShopManager;
import cat.nyaa.hmarket.signshop.SignShopType;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.HexColorUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignShopHandle extends CommandReceiver {


    /**
     * @param plugin for logging purpose only
     * @param _i18n  i18n
     */
    public SignShopHandle(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @SubCommand(value = "create")
    public void onCreate(CommandSender sender, Arguments args) {
        var player = asPlayer(sender);
        var block = player.getTargetBlockExact(7);  // what the fuck?
        if (block == null || !(block.getState() instanceof Sign sign)) {
            I18n.send(sender, "command.shop.isnotSign");
            return;
        }
        if (Arrays.stream(sign.getLines()).anyMatch(l -> !l.equals(""))) {
            I18n.send(sender, "command.shop.isnotEmptySign");
            return;
        }
        if (SignShopManager.getInstance().isSignShop(block.getLocation())) {
            I18n.send(sender, "command.shop.isShop");
            return;
        }
        try {
            var type = SignShopType.valueOf(args.nextString().toUpperCase());
            List<String> lores = new ArrayList<>();
            while (args.top() != null) {
                lores.add(HexColorUtils.hexColored(args.nextString()));
            }
            SignShop shop = new SignShop(player.getUniqueId(), type, Iterables.toArray(lores, String.class), sign.getLocation());
            SignShopManager.getInstance().addSignShop(shop);
            shop.applySign();
            I18n.send(sender, "command.shop.createSuccessful");
        } catch (IllegalArgumentException e) {
            I18n.send(sender, "command.shop.invalidArgs");
        }
    }

    @SubCommand(value = "buy", permission = "hmarket.command.shop.buy")
    public void onBuy(CommandSender sender, Arguments args) {
        var player = asPlayer(sender);
        var block = player.getTargetBlockExact(7);

        if (block == null || !(block.getState() instanceof Sign) || !SignShopManager.getInstance().isSignShop(block.getLocation())) {
            new Message("").send(sender);
            return;
        }
        var shop = SignShopManager.getInstance().getSignShopByLocation(block.getLocation());
        if (!shop.getOwner().equals(player.getUniqueId()) || shop.getType() != SignShopType.BUY) {
            new Message("").send(sender);
            return;
        }
        var handItem = player.getInventory().getItemInMainHand().clone();


        if (handItem.getType().isAir()) {
            new Message("").send(sender);
            return;
        }

        var price = args.nextDouble();
        var shopItem = new ShopItem(player.getUniqueId(), price, handItem, ShopItemType.SIGNSHOP_BUY);
        ShopItemManager.getInstance().addShopItem(shopItem);

        //TODO: limit


        new Message("").send(sender);
    }

    @SubCommand(value = "sell", permission = "hmarket.command.shop.sell")
    public void sell(CommandSender sender, Arguments args) {
        var player = asPlayer(sender);

        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));


    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }
}
