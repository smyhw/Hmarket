package cat.nyaa.hmarket.ui;

import cat.nyaa.aolib.aoui.IBaseUI;
import cat.nyaa.aolib.aoui.PageUI;
import cat.nyaa.aolib.aoui.item.IUiItem;
import cat.nyaa.aolib.utils.TaskUtils;
import cat.nyaa.hmarket.Hmarket;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class HMPageUi extends PageUI {
    private final UUID shopId;

    public HMPageUi(UUID shopId, Consumer<IBaseUI> updateConsumer, @NotNull String uiTitle) {
        super(Lists.newArrayList(), updateConsumer, uiTitle);
        this.shopId = shopId;
        updateShopItem();
    }

    public void updateShopItem() {
        var hmApi = Hmarket.getAPI();
        if (hmApi == null) return;
        hmApi.getMarketAPI().getShopItems(shopId).thenAccept((items) -> TaskUtils.async.callSync(() ->
                this.setAllUiItem(items.stream().map((item) -> (IUiItem) new HmUiShopItem(item)).toList())));
    }
}
