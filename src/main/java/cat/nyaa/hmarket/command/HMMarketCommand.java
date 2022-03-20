package cat.nyaa.hmarket.command;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.HMarketAPI;
import cat.nyaa.hmarket.api.exception.NotEnoughItemsException;
import cat.nyaa.hmarket.api.exception.NotEnoughMoneyException;
import cat.nyaa.hmarket.api.exception.NotEnoughSpaceException;
import cat.nyaa.hmarket.utils.HMUiUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HMMarketCommand extends CommandReceiver {
    private final CommandManager commandManager;

    /**
     * @param commandManager for logging purpose only
     * @param _i18n  i18n
     */
    public HMMarketCommand(CommandManager commandManager, ILocalizer _i18n) {
        super(commandManager.getPlugin(), _i18n);
        this.commandManager = commandManager;
    }

    @SubCommand(value = "view", isDefaultCommand = true, permission = "hmarket.mall")
    public void view(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        HMUiUtils.openShopUi(player, HMarketAPI.systemShopId, "SHOP");
    }

    @SubCommand(value = "offer", permission = "hmarket.mall")
    public void offer(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        var hmApi = commandManager.getPlugin().getHMarketAPI();
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
        }catch (NotEnoughMoneyException e){
            HMI18n.send(sender, "command.not-enough-money");
        } catch (NotEnoughSpaceException e) {
            HMI18n.send(sender, "command.not-enough-space");
        }

    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}
