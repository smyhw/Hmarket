CREATE TABLE IF NOT EXISTS shop_item
(
    itemId  INTEGER
        PRIMARY KEY AUTOINCREMENT,
    itemNbt TEXT NOT NULL,
    amount INTEGER NOT NULL,
    owner   VARCHAR NOT NULL,
    market  VARCHAR NOT NULL,
    price   DOUBLE NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT NOT NULL,
    description TEXT
);
CREATE TABLE IF NOT EXISTS shop_location_v2
(
    blockX  INTEGER NOT NULL,
    blockY  INTEGER NOT NULL,
    blockZ  INTEGER NOT NULL,
    world   VARCHAR NOT NULL,
    type    VARCHAR NOT NULL,
    owner   VARCHAR NOT NULL,
    market  VARCHAR NOT NULL,
    CONSTRAINT shop_location_id PRIMARY KEY (blockX, blockY, blockZ, world)
);