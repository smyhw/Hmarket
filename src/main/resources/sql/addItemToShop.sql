INSERT INTO shop_item (itemNbt, amount, owner, market, price
	, createdAt, updatedAt)
SELECT *
FROM (
    VALUES(?, ?, ?, ?, ?, ?, ?)
)
WHERE (
	SELECT COUNT(*)
	FROM shop_item
	WHERE market = ? AND owner = ?
) < ?;