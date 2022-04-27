package cat.nyaa.hmarket.utils;

import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.hmarket.Hmarket;

import java.util.logging.Logger;

public class HMLogUtils {
    public static void logInfo(String message) {
        var plugin = Hmarket.getInstance();
        if (plugin == null) return;
        TaskUtils.async.callSync(() -> plugin.getLogger().info(message));
    }

    public static void logWarning(String message) {
        var plugin = Hmarket.getInstance();

        TaskUtils.async.callSync(() -> {
            if (plugin != null) {
                plugin.getLogger().warning(message);
            } else {
                Logger.getAnonymousLogger().warning(message);
            }
        });
    }

    public static void logError(String message) {
        var plugin = Hmarket.getInstance();
        if (plugin == null) return;
        TaskUtils.async.callSync(() -> plugin.getLogger().severe(message));
    }
}
