CREATE TABLE IF NOT EXISTS demo_users
(
    user_id
    VARCHAR
(
    40
) PRIMARY KEY, display_name VARCHAR
(
    120
), role VARCHAR
(
    20
), demo_only BOOLEAN DEFAULT TRUE);

INSERT INTO demo_users(user_id, display_name, role)
SELECT 'demo-supplier', 'Aarav Supplier', 'SUPPLIER' WHERE NOT EXISTS (SELECT 1 FROM demo_users WHERE user_id='demo-supplier');
INSERT INTO demo_users(user_id, display_name, role)
SELECT 'demo-buyer', 'Northstar Buyer', 'BUYER' WHERE NOT EXISTS (SELECT 1 FROM demo_users WHERE user_id='demo-buyer');
INSERT INTO demo_users(user_id, display_name, role)
SELECT 'demo-finance', 'Finance Operator', 'FINANCE' WHERE NOT EXISTS (SELECT 1 FROM demo_users WHERE user_id='demo-finance');
INSERT INTO demo_users(user_id, display_name, role)
SELECT 'demo-admin', 'Platform Admin', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM demo_users WHERE user_id='demo-admin');
