CREATE TABLE IF NOT EXISTS payments
(
    payment_id
    VARCHAR
(
    40
) PRIMARY KEY, financing_id VARCHAR
(
    40
) NOT NULL UNIQUE, invoice_id VARCHAR
(
    40
) NOT NULL, amount NUMERIC
(
    18,
    2
), currency VARCHAR
(
    3
), payment_reference VARCHAR
(
    80
), status VARCHAR
(
    30
), disbursed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);
