INSERT INTO shop_location_v2 (blockX, blockY, blockZ, world, type
, owner, market)
SELECT *
FROM VALUES(?, ?, ?, ?, ?, ?, ?)
WHERE (
	SELECT COUNT(*)
	FROM shop_location_v2
	WHERE owner = ?
) < ?;