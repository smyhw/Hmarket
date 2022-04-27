package cat.nyaa.hmarket.task;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.hmarket.utils.TimeUtils;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateItemTask extends BukkitRunnable {
    private final int delay;

    public UpdateItemTask(int delay) {
        this.delay = delay;
    }

    @Override
    public void run() {
        var api = Hmarket.getAPI();
        if (api == null) return;
        api.getMarketAPI().updateItem(TimeUtils.getUnixTimeStampNow() - (delay * 50L), TimeUtils.getUnixTimeStampNow());
    }
}
