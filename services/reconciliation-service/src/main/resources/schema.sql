CREATE TABLE IF NOT EXISTS reconciliations
(
    reconciliation_id
    VARCHAR
(
    40
) PRIMARY KEY, payment_id VARCHAR
(
    40
) NOT NULL UNIQUE, invoice_id VARCHAR
(
    40
) NOT NULL, financing_id VARCHAR
(
    40
), matched_amount NUMERIC
(
    18,
    2
), currency VARCHAR
(
    3
), match_status VARCHAR
(
    30
), reason CLOB, reconciled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);
