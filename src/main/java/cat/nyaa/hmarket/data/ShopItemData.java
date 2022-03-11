package cat.nyaa.hmarket.data;

public record ShopItemData(
        int itemId,
        String itemNbt,
        String owner, //owner id
        String market, //market uuid
        double price,
        long createdAt, //time
        long updatedAt//time
) {
}
