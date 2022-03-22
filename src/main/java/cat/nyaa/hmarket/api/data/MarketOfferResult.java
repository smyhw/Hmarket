package cat.nyaa.hmarket.api.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MarketOfferResult(@NotNull Optional<Integer> itemId, @NotNull MarketOfferReason reason) {
    @Contract("_ -> new")
    public static @NotNull MarketOfferResult fail(MarketOfferReason reason) {
        if (reason == MarketOfferReason.SUCCESS) {
            return new MarketOfferResult(Optional.empty(), MarketOfferReason.UNKNOWN);
        }
        return new MarketOfferResult(Optional.empty(), reason);
    }

    public static @NotNull MarketOfferResult success( int itemId) {
        return new MarketOfferResult(Optional.of(itemId), MarketOfferReason.SUCCESS);
    }

    public boolean isSuccess() {
        return reason == MarketOfferReason.SUCCESS;
    }

    public enum MarketOfferReason {
        SUCCESS,
        NOT_ENOUGH_ITEMS,
        NOT_ENOUGH_MONEY,
        TASK_FAILED,
        NOT_ENOUGH_SPACE,
        DATABASE_ERROR,
        INVALID_PRICE,
        UNKNOWN
    }
}

