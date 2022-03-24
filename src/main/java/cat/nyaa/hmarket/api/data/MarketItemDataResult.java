package cat.nyaa.hmarket.api.data;

import cat.nyaa.hmarket.data.ShopItemData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MarketItemDataResult(@NotNull MarketItemDataStatus status, Optional<ShopItemData> data) {
    @Contract("_ -> new")
    public static @NotNull MarketItemDataResult fail(MarketItemDataStatus status) {
        if (status == MarketItemDataStatus.SUCCESS) throw new IllegalArgumentException("status can't be SUCCESS");
        return new MarketItemDataResult(status, Optional.empty());
    }

    @Contract("_ -> new")
    public static @NotNull MarketItemDataResult success(ShopItemData shopItemData) {
        return new MarketItemDataResult(MarketItemDataStatus.SUCCESS, Optional.of(shopItemData));
    }

    public boolean isSuccess() {
        return status == MarketItemDataStatus.SUCCESS && data.isPresent();
    }

    public enum MarketItemDataStatus {
        NOT_FOUND, SUCCESS
    }
}
