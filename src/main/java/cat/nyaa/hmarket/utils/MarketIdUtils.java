package cat.nyaa.hmarket.utils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MarketIdUtils {
    private static final UUID systemShopId = UUID.nameUUIDFromBytes("HM_system_shop".getBytes());
    private static final UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static UUID parseMarketId(@NotNull String marketId) {
        if (marketId.equalsIgnoreCase("system")) {
            return systemShopId;
        } else {
            return parseUUID(marketId);
        }
    }

    public static UUID parseUUID(@NotNull String stringUUID) {
        UUID result = null;
        try {
            result = UUID.fromString(stringUUID);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (result == null) {
            HMLogUtils.logWarning("can't parse id: " + stringUUID);
            return getUnknownId();
        }
        return result;
    }

    private static UUID getUnknownId() {
        HMLogUtils.logWarning("get unknown id");
        return unknownId;
    }

    public static UUID getSystemShopId() {
        return systemShopId;
    }
}
