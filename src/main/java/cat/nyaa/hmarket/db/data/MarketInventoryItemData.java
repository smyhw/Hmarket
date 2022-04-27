package cat.nyaa.hmarket.db.data;

import java.util.UUID;

public record MarketInventoryItemData(int itemId, UUID marketUUID, String itemNbt) {

}
