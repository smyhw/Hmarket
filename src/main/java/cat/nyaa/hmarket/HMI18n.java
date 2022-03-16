package cat.nyaa.hmarket;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HMI18n extends LanguageRepository {
    private final Hmarket plugin;
    private final String language;
    private static HMI18n instance;

    public HMI18n(Hmarket plugin, String language) {
        instance = this;
        this.plugin = plugin;
        this.language = language;
        load();
    }

    @Contract(pure = true)
    public static String format(String key, Object... args) {
        if (instance == null) return "<Not initialized>";
        return instance.getFormatted(key, args);
    }

    @Contract(pure = true)
    public static String substitute(String key, Object... args) {
        if (instance == null) return "<Not initialized>";
        return instance.getSubstituted(key, args);
    }

    public static void sendPlayerSync(UUID playerId, String key, Object... args) {
        if (instance == null) return;
        TaskUtils.async.callSyncAndGet(() -> {
                    var player = Bukkit.getPlayer(playerId);
                    if (player == null) return null;
                    HMI18n.send(player, key, args);
                    return null;
                }
        );
    }

    public static void send(@NotNull CommandSender recipient, String key, Object... args) {
        recipient.sendMessage(format(key, args));
    }


    @Override
    protected Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    protected String getLanguage() {
        return this.language;
    }
}
