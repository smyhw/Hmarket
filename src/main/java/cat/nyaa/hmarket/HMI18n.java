package cat.nyaa.hmarket;

import cat.nyaa.aolib.message.AoMessage;
import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HMI18n extends LanguageRepository {
    private static HMI18n instance;
    private final Hmarket plugin;
    private final String language;

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

    public static void sendSync(UUID playerId, String key, Object... args) {
        if (instance == null) return;
        TaskUtils.async.callSync(() -> {
                    var player = Bukkit.getPlayer(playerId);
                    if (player == null) return;
                    HMI18n.send(player, key, args);
                }
        );
    }
    public static void sendSubstituteSync(UUID playerId, String key, Object... args) {
        if (instance == null) return;
        TaskUtils.async.callSync(() -> {
                    var player = Bukkit.getPlayer(playerId);
                    if (player == null) return;
                    HMI18n.sendSubstitute(player, key, args);
                }
        );
    }
    

    public static void send(@NotNull CommandSender recipient, String key, Object... args) {
        if (recipient instanceof Player player && !player.isOnline()) {
            var aoMessage = AoMessage.getInstance();
            if (aoMessage != null) {
                aoMessage.sendMessageTo(player.getUniqueId(),format(key, args));
            }
        } else {
            recipient.sendMessage(format(key, args));
        }
    }
    public static void sendSubstitute(@NotNull CommandSender recipient, String key, Object... args) {
        if (recipient instanceof Player player && !player.isOnline()) {
            var aoMessage = AoMessage.getInstance();
            if (aoMessage != null) {
                aoMessage.sendMessageTo(player.getUniqueId(),substitute(key, args));
            }
        } else {
            recipient.sendMessage(substitute(key, args));
        }
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
