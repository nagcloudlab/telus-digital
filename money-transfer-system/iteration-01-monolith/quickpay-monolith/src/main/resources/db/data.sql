-- Insert test users
INSERT INTO users (email, password, full_name, mobile_number, enabled) VALUES
('ramesh@example.com', '$2a$10$xQ7VXgZMXoZjE6v7JZhDJ.KHjl7JQE6SxZ2kxF9JZjE6v7JZhDJ.K', 'Ramesh Kumar', '9876543210', true),
('priya@example.com', '$2a$10$xQ7VXgZMXoZjE6v7JZhDJ.KHjl7JQE6SxZ2kxF9JZjE6v7JZhDJ.K', 'Priya Sharma', '9876543211', true),
('amit@example.com', '$2a$10$xQ7VXgZMXoZjE6v7JZhDJ.KHjl7JQE6SxZ2kxF9JZjE6v7JZhDJ.K', 'Amit Patel', '9876543212', true)
ON CONFLICT (email) DO NOTHING;

-- Insert test accounts
INSERT INTO accounts (account_number, user_id, balance, currency, status) VALUES
('ACC123456', (SELECT id FROM users WHERE email = 'ramesh@example.com'), 50000.00, 'INR', 'ACTIVE'),
('ACC987654', (SELECT id FROM users WHERE email = 'priya@example.com'), 30000.00, 'INR', 'ACTIVE'),
('ACC555777', (SELECT id FROM users WHERE email = 'amit@example.com'), 75000.00, 'INR', 'ACTIVE')
ON CONFLICT (account_number) DO NOTHING;