package cat.nyaa.hmarket.schedule

import cat.nyaa.hmarket.shopitem.ItemCacheManager

class MemDbSyncTask: Runnable {
    override fun run() {
        ItemCacheManager.getInstance().syncDirty();
    }
}