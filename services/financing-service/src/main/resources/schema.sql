CREATE TABLE IF NOT EXISTS financing_applications
(
    financing_id
    VARCHAR
(
    40
) PRIMARY KEY, invoice_id VARCHAR
(
    40
) NOT NULL UNIQUE, supplier_id VARCHAR
(
    40
), buyer_id VARCHAR
(
    40
), requested_amount NUMERIC
(
    18,
    2
), approved_amount NUMERIC
(
    18,
    2
), discount_rate NUMERIC
(
    9,
    6
), currency VARCHAR
(
    3
), status VARCHAR
(
    30
), created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);
