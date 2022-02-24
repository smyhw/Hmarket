package cat.nyaa.hmarket.command;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.command.sub.MallHandle;
import cat.nyaa.hmarket.command.sub.SignShopHandle;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;

public class CommandHandle extends CommandReceiver {

    @SubCommand(value = "shop", permission = "")
    public SignShopHandle signShopHandle;


    /**
     * @param plugin for logging purpose only
     * @param _i18n  i18n
     */
    public CommandHandle(Hmarket plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }
}