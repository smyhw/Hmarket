package cat.nyaa.hmarket.command;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.utils.HMUiUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HMCommandHeader extends CommandReceiver {
    private final Hmarket plugin;

    /**
     * @param plugin for logging purpose only
     * @param _i18n  i18n
     */
    public HMCommandHeader(Hmarket plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
    }

    @SubCommand(value = "view", isDefaultCommand = true, permission = "hmarket.command.view")
    public void view(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        HMUiUtils.openShopUi(player, HMarketAPI.systemShopId, "SHOP");
    }

    @SubCommand(value = "offer", permission = "hmarket.command.offer")
    public void offer(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        var hmApi = plugin.getHMarketAPI();
        if (hmApi == null) return;
        var price = BigDecimal.valueOf(args.nextDouble()).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        if (price <= 0) {
            HMI18n.send(sender, "command.invalid-price");
            return;
        }
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir() || item.getAmount() <= 0) {
            HMI18n.send(sender, "command.invalid-item-in-hand");
            return;
        }
        try {
            hmApi.offer(player, hmApi.getSystemShopId(), item, price);
        } catch (NotEnoughItemsException e) {
            HMI18n.send(sender, "command.not-enough-item-in-hand");
        }

    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
