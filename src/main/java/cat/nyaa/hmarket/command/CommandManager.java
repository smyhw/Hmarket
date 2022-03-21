package cat.nyaa.hmarket.command;

import cat.nyaa.hmarket.HMI18n;
import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import org.jetbrains.annotations.NotNull;

public class CommandManager {
    public final HMMarketCommand marketCommand;
    public final HMMainCommand mainCommand;
    private final Hmarket plugin;

    public CommandManager(Hmarket plugin, HMI18n i18n) {
        this.plugin = plugin;

        this.marketCommand = new HMMarketCommand(this, i18n);
        this.mainCommand = new HMMainCommand(this, i18n);
        regCommand(plugin, "hmarket_market", marketCommand);
        regCommand(plugin, "hmarket", mainCommand);
    }

    public Hmarket getPlugin() {
        return plugin;
    }

    private void regCommand(@NotNull Hmarket plugin, @NotNull String commandName, @NotNull CommandReceiver commandReceiver) {
        var pluginCommand = plugin.getCommand(commandName);
        if (pluginCommand == null) {
            plugin.getLogger().warning("Command registration failed : " + commandName + " not found.");
            return;
        }
        pluginCommand.setExecutor(commandReceiver);
        pluginCommand.setTabCompleter(commandReceiver);
    }
}
