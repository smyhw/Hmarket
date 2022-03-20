package cat.nyaa.hmarket.task;

import cat.nyaa.hmarket.Hmarket;
import org.bukkit.scheduler.BukkitTask;

public class HMTaskManager {
    private final BukkitTask updateItemTask;

    public HMTaskManager(Hmarket plugin) {
        this.updateItemTask = new UpdateItemTask(3600*20).runTaskTimer(plugin,3600*20,3600*20);
    }
   public void destructor(){
        this.updateItemTask.cancel();
    }
}
