package cat.nyaa.hmarket.api.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MarketOfferResult(@NotNull Optional<Integer> itemId,
                                @NotNull MarketOfferResult.MarketOfferStatus status) {
    @Contract("_ -> new")
    public static @NotNull MarketOfferResult fail(MarketOfferStatus reason) {
        if (reason == MarketOfferStatus.SUCCESS) {
            throw new IllegalArgumentException("status can't be SUCCESS");
        }
        return new MarketOfferResult(Optional.empty(), reason);
    }

    public static @NotNull MarketOfferResult success(int itemId) {
        return new MarketOfferResult(Optional.of(itemId), MarketOfferStatus.SUCCESS);
    }

    public boolean isSuccess() {
        return status == MarketOfferStatus.SUCCESS;
    }

    public enum MarketOfferStatus {
        SUCCESS,
        NOT_ENOUGH_ITEMS,
        NOT_ENOUGH_MONEY,
        TASK_FAILED,
        NOT_ENOUGH_SPACE,
        DATABASE_ERROR,
        INVALID_PRICE
    }
}

