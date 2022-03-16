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