package cat.nyaa.hmarket.api.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MarketBuyResult {
    private boolean modified;
    private final MarketBuyStatus status;

    public MarketBuyResult(@NotNull MarketBuyStatus status, boolean modified) {
        this.status = status;
        this.modified = modified;
    }

    @Contract("_ -> new")
    public static @NotNull MarketBuyResult fail(@NotNull MarketBuyStatus status) {
        return fail(status, false);
    }

    @Contract("_, _ -> new")
    public static @NotNull MarketBuyResult fail(@NotNull MarketBuyStatus status, boolean modified) {
        if (status == MarketBuyStatus.SUCCESS) throw new IllegalArgumentException("status can't be SUCCESS");
        return new MarketBuyResult(status, modified);
    }

    @Contract(" -> new")
    public static @NotNull MarketBuyResult success() {
        return new MarketBuyResult(MarketBuyStatus.SUCCESS, true);
    }

    public boolean isSuccess() {
        return status == MarketBuyStatus.SUCCESS;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean b) {
        this.modified = b;
    }

    public MarketBuyStatus getStatus() {
        return status;
    }

    public enum MarketBuyStatus {
        SUCCESS,
        OUT_OF_STOCK,
        NOT_ENOUGH_MONEY,
        TASK_FAILED,
        ITEM_NOT_FOUND,
        TRANSACTION_ERROR,
        WRONG_MARKET,
        CANNOT_REMOVE_ITEM
    }
}
