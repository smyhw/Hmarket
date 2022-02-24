package cat.nyaa.hmarket.schedule

import cat.nyaa.hmarket.Hmarket
import org.bukkit.Bukkit

class ScheduleManager private constructor() {
    val tasks: List<Int> = ArrayList()
    private lateinit var hmarket: Hmarket;
    private fun register() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(hmarket, MemDbSyncTask(), 30 * 20, 60 * 20);
    }

    fun init(hmarket: Hmarket) {
        this.hmarket = hmarket;
        register()
    }

    companion object {
        val instance: ScheduleManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ScheduleManager()
        }
    }
}