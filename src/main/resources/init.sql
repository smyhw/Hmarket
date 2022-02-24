create table if not exists items
(
    nbt   TEXT,
    price REAL,
    owner TEXT,
    type  TEXT,
    id    INTEGER
        constraint items_pk
            primary key autoincrement
);

create table if not exists locations
(
    world TEXT,
    x     REAL,
    y     REAL,
    z     REAL,
    owner TEXT,
    lores TEXT,
    type  TEXT,
    id    INTEGER
        constraint locations_pk
            primary key autoincrement
);

create table  if not exists store
(
    amount INTEGER,
    owner  TEXT,
    nbt    TEXT
);

