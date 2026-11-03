CREATE TABLE documents (
    order_id   VARCHAR(64)  PRIMARY KEY,
    sku        VARCHAR(64)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    indexed_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
