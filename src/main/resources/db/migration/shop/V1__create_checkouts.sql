CREATE TABLE checkouts (
    id          VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    sku         VARCHAR(64) NOT NULL,
    quantity    INTEGER     NOT NULL,
    status      VARCHAR(16) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
