package cat.nyaa.hmarket.command.sub;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.utils.CommandUtils;
import cat.nyaa.hmarket.utils.HMMathUtils;
import cat.nyaa.hmarket.utils.PlayerNameUtils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HMSignShopCommand extends CommandReceiver {


    /**
     * @param plugin for logging purpose only
     * @param _i18n  i18n
     */
    public HMSignShopCommand(Hmarket plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @SubCommand(value = "shops", permission = "hmarket.my")
    public void list(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player player)) {
            HMI18n.send(sender, "command.only-player-can-do");
            return;
        }
        UUID targetId = null;
        if (sender.isOp()) {
            var name = args.next();
            if (name != null) {
                targetId = CommandUtils.receiveCommand.getPlayerUUIDByStr(name, sender);
            }
        }
        if (targetId == null) {
            targetId = player.getUniqueId();
        }
        var playerId = player.getUniqueId();
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return;
        var shopLocation = hmApi.getShopLocationApi();
        shopLocation.getLocationDataByOwner(targetId).thenAccept(
                locationDataList -> {
                    if (locationDataList.isEmpty()) {
                        HMI18n.sendSync(playerId, "command.database_error");
                        return;
                    }
                    var locationDataList1 = locationDataList.get();
                    if (locationDataList1.isEmpty()) {
                        HMI18n.sendSync(playerId, "command.shop.list.empty");
                        return;
                    }
                    for (var locationData : locationDataList1) {
                        HMI18n.sendSubstituteSync(playerId, "command.shop.list.info",
                                "world", locationData.world(),
                                "x", locationData.blockX(),
                                "y", locationData.blockY(),
                                "z", locationData.blockZ(),
                                "owner", PlayerNameUtils.getPlayerNameById(locationData.owner()),
                                "market", locationData.market(),
                                "type", locationData.type()
                        );

                    }

                }
        );
    }

    @SubCommand(value = "offer", permission = "hmarket.my")
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

//        var targetBlock = player.getTargetBlockExact(10);
//        Location targetLocation = null;
//        if (targetBlock == null || !(targetBlock.getState() instanceof Sign)) {
//            var rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, entity -> entity instanceof ItemFrame);
//            if (rayTraceResult != null) {
//                var entity = rayTraceResult.getHitEntity();
//                if (entity instanceof ItemFrame) {
//                    targetLocation = entity.getLocation();
//                }
//            }
//        } else {
//            targetLocation = targetBlock.getLocation();
//        }
//        if (targetLocation == null || !targetLocation.isWorldLoaded() || targetLocation.getWorld() == null) {
//            HMI18n.send(sender, "command.invalid-target-location");
//            return;
//        }
//
//        var blockLocationData = BlockLocationData.fromLocation(targetLocation);
//
//        hmApi.getShopLocationApi().getLocationData(blockLocationData).ifPresentOrElse(
//                locationData -> {
//                    if (!locationData.owner().equals(player.getUniqueId())) {
//                        HMI18n.send(sender, "command.shop.not-owner");
//                    } else {
//                        hmApi.getMarketAPI().commandOffer(player, player.getUniqueId(), item, price);
//                    }
//                },
//                () -> HMI18n.send(sender, "command.invalid-target-location")
//        );
        hmApi.getMarketAPI().commandOffer(player, player.getUniqueId(), item, price);
    }

    @Override
    public String getHelpPrefix() {
        return "shop";
    }
}
