CREATE TABLE payments (
    order_id   VARCHAR(64)   PRIMARY KEY,
    amount     NUMERIC(12,2) NOT NULL,
    status     VARCHAR(32)   NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
