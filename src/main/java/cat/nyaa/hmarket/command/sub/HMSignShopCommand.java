package cat.nyaa.hmarket.command.sub;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.api.data.BlockLocationData;
import cat.nyaa.hmarket.utils.HMMathUtils;
import cat.nyaa.hmarket.utils.MarketIdUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public class HMSignShopCommand extends CommandReceiver {


    private final Hmarket plugin;

    /**
     * @param plugin for logging purpose only
     * @param _i18n  i18n
     */
    public HMSignShopCommand(Hmarket plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
    }

    @SubCommand(value = "sell", permission = "hmarket.shop")
    public void sell(CommandSender sender, Arguments args) {
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
            HMI18n.send(sender, "command.invalid-item-in-hand");
            return;
        }

        var targetBlock = player.getTargetBlockExact(10);
        Location targetLocation = null;
        if (targetBlock == null || !(targetBlock.getState() instanceof Sign)) {
            var rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, entity -> entity instanceof ItemFrame);
            if (rayTraceResult != null) {
                var entity = rayTraceResult.getHitEntity();
                if (entity instanceof ItemFrame) {
                    targetLocation = entity.getLocation();
                }
            }
        } else {
            targetLocation = targetBlock.getLocation();
        }
        if (targetLocation == null || !targetLocation.isWorldLoaded() || targetLocation.getWorld() == null) {
            HMI18n.send(sender, "command.invalid-target-location");
            return;
        }

        var blockLocationData = BlockLocationData.fromLocation(targetLocation);

        hmApi.getShopLocationApi().getLocationData(blockLocationData).ifPresentOrElse(
                locationData -> {
                    if (locationData.owner() != player.getUniqueId()) {
                        HMI18n.send(sender, "command.shop.not-owner");
                    } else {
                        hmApi.getMarketAPI().commandOffer(player, MarketIdUtils.getSystemShopId(), item, price);
                    }
                },
                () -> HMI18n.send(sender, "command.invalid-target-location")
        );


    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }
}
