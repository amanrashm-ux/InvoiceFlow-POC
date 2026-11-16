CREATE TABLE IF NOT EXISTS parties (party_id VARCHAR(40) PRIMARY KEY, party_type VARCHAR(20) NOT NULL, legal_name VARCHAR(180) NOT NULL, email VARCHAR(180), country_code VARCHAR(2) DEFAULT 'IN', status VARCHAR(20) DEFAULT 'ACTIVE');

INSERT INTO parties(party_id,party_type,legal_name,email,country_code) SELECT 'SUP-001','SUPPLIER','Aarav Industrial Components Pvt Ltd','accounts@aarav-demo.example','IN' WHERE NOT EXISTS (SELECT 1 FROM parties WHERE party_id='SUP-001');
INSERT INTO parties(party_id,party_type,legal_name,email,country_code) SELECT 'SUP-002','SUPPLIER','Delta Packaging Works','finance@delta-demo.example','IN' WHERE NOT EXISTS (SELECT 1 FROM parties WHERE party_id='SUP-002');
INSERT INTO parties(party_id,party_type,legal_name,email,country_code) SELECT 'SUP-003','SUPPLIER','Eastern Textiles LLP','billing@eastern-demo.example','IN' WHERE NOT EXISTS (SELECT 1 FROM parties WHERE party_id='SUP-003');
INSERT INTO parties(party_id,party_type,legal_name,email,country_code) SELECT 'BUY-001','BUYER','Northstar Retail India Ltd','ap@northstar-demo.example','IN' WHERE NOT EXISTS (SELECT 1 FROM parties WHERE party_id='BUY-001');
INSERT INTO parties(party_id,party_type,legal_name,email,country_code) SELECT 'BUY-002','BUYER','Meridian Manufacturing Ltd','finance@meridian-demo.example','IN' WHERE NOT EXISTS (SELECT 1 FROM parties WHERE party_id='BUY-002');
