-- Sample Accounts for Testing
INSERT INTO accounts (account_number, account_holder_name, balance, currency, status, account_type, daily_limit, daily_transferred, minimum_balance, created_at, updated_at, version) 
VALUES 
('ACC001', 'John Doe', 10000.00, 'USD', 'ACTIVE', 'SAVINGS', 10000.00, 0.00, 100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('ACC002', 'Jane Smith', 5000.00, 'USD', 'ACTIVE', 'SAVINGS', 10000.00, 0.00, 100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('ACC003', 'Bob Johnson', 50000.00, 'USD', 'ACTIVE', 'CURRENT', 50000.00, 0.00, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('ACC004', 'Alice Williams', 1000.00, 'USD', 'INACTIVE', 'SAVINGS', 10000.00, 0.00, 100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('ACC005', 'Charlie Brown', 100.00, 'USD', 'ACTIVE', 'SAVINGS', 5000.00, 0.00, 100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);