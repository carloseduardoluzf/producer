CREATE TABLE IF NOT EXISTS orders (
    id      BIGSERIAL PRIMARY KEY,
    product VARCHAR(200) NOT NULL,
    qty     INT          NOT NULL,
    price   NUMERIC(12,2),
    created_at TIMESTAMPTZ DEFAULT NOW()
);
