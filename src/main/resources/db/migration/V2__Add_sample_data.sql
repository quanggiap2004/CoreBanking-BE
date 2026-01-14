-- Sample Data Migration (Optional - for development/testing)
-- Flyway Version: V2
-- Description: Adds sample users and accounts for testing

-- Note: Uncomment this file content only if you want sample data
-- For production, keep this file empty or delete it

/*
-- Sample User 1 (password: password123)
INSERT INTO users (username, password_hash, full_name, email, phone, kyc_verified, status)
VALUES (
    'demo_user',
    '$2a$10$xZV5h7jk9X8Y6wQ3rN2tLOeH5xJ8kM9pL4qW3rT6yU8vP1sN4oR2m',  -- BCrypt hash of 'password123'
    'Demo User',
    'demo@example.com',
    '+1234567890',
    true,
    'ACTIVE'
);

-- Sample Account for Demo User
INSERT INTO accounts (account_number, user_id, balance, account_type, status, interest_rate)
VALUES (
    '100000000001',
    (SELECT id FROM users WHERE username = 'demo_user'),
    1000.0000,
    'SAVINGS',
    'ACTIVE',
    0.0350
);
*/

-- Keep this file for future data migrations
-- Example: Adding default configuration data, lookup tables, etc.
