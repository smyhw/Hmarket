package cat.nyaa.hmarket.command;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.command.sub.HMSignShopCommand;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;

public class HMMainCommand extends CommandReceiver {
    private final CommandManager commandManager;

    public HMMainCommand(CommandManager commandManager, HMI18n i18n) {
        super(commandManager.getPlugin(), i18n);
        this.commandManager = commandManager;
        this.shop = new HMSignShopCommand(commandManager, i18n);
    }

    @SubCommand(value = "shop", permission = "hmarket.shop")
    public HMSignShopCommand shop;

    @SubCommand(value = "mall", permission = "hmarket.mall")
    public void mall(CommandSender sender, Arguments args) {
        commandManager.marketCommand.view(sender, args);
    }

    @SubCommand(value = "reload", permission = "hmarket.reload")
    public void reload(CommandSender sender, Arguments args) {
        commandManager.getPlugin().onReload();
        sender.sendMessage("HMarket reloaded.");
    }


    @Override
    public String getHelpPrefix() {
        return "";
    }
}
