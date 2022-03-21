package cat.nyaa.hmarket.ui;

import cat.nyaa.aolib.aoui.IBaseUI;
import cat.nyaa.aolib.aoui.PageUI;
import cat.nyaa.aolib.aoui.item.IUiItem;
import cat.nyaa.aolib.network.data.DataClickType;
import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.hmarket.Hmarket;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class HMPageUi extends PageUI {
    private final UUID shopId;
    private boolean locked;

    public HMPageUi(UUID shopId, Consumer<IBaseUI> updateConsumer, @NotNull String uiTitle) {
        super(Lists.newArrayList(), updateConsumer, uiTitle);
        this.shopId = shopId;
        updateShopItem();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    private void updateShopItem() {
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return;
        hmApi.getShopItems(shopId).thenAccept((items) -> TaskUtils.async.callSyncAndGet(() -> {
            this.setAllUiItem(
                    items.stream().map((item) -> (IUiItem) new HmUiShopItem(item, this::updateShopItem, this::setLocked)).toList());
            return null;
        }));
    }

    @Override
    public void onWindowClick(int slotNum, int buttonNum, DataClickType clickType, Player player) {
        if (this.locked) return;
        super.onWindowClick(slotNum, buttonNum, clickType, player);
    }
}
