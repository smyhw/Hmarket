package cat.nyaa.hmarket.command;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.command.sub.HMSignShopCommand;
import cat.nyaa.hmarket.utils.HMMathUtils;
import cat.nyaa.hmarket.utils.MarketIdUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static cat.nyaa.hmarket.HMI18n.getComponentOfItem;

public class HMMarketCommand extends CommandReceiver {
    private final CommandManager commandManager;

    /**
     * @param commandManager for logging purpose only
     * @param _i18n          i18n
     */
    public HMMarketCommand(CommandManager commandManager, ILocalizer _i18n) {
        super(commandManager.getPlugin(), _i18n);
        this.commandManager = commandManager;
    }

    @SubCommand(value = "my", permission = "hmarket.my")
    public HMSignShopCommand shop;

    @SubCommand(value = "view", isDefaultCommand = true, permission = "hmarket.mall")
    public void view(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        Hmarket.getInstance().getViewServer().createViewForPlayer(player,MarketIdUtils.getSystemShopId(),
                HMI18n.format("info.ui.title.shop.system"));
        Hmarket.getInstance().getViewServer().openViewForPlayer(player);
    }

    @SubCommand(value = "offer", permission = "hmarket.mall")
    public void offer(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return;
        var price = HMMathUtils.round(args.nextDouble(), 2);
        if (price <= 0 || price >= Integer.MAX_VALUE) {
            HMI18n.send(sender, "command.invalid-price");
            return;
        }
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir() || item.getAmount() <= 0) {
            Bukkit.broadcast(HMI18n.format("info.market.sell", player.getName())
                    .append(getComponentOfItem(item)));
            return;
        }
        hmApi.getMarketAPI().commandOffer(player, MarketIdUtils.getSystemShopId(), item, price);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
