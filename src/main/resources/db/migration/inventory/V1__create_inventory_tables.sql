CREATE TABLE stock (
    sku      VARCHAR(64) PRIMARY KEY,
    quantity INTEGER     NOT NULL,
    reserved INTEGER     NOT NULL DEFAULT 0
);

CREATE TABLE holds (
    id         VARCHAR(64) PRIMARY KEY,
    sku        VARCHAR(64) NOT NULL,
    quantity   INTEGER     NOT NULL,
    status     VARCHAR(32) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
