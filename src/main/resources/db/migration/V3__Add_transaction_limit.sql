-- Add Transaction Limit to Users
-- Flyway Version: V3
-- Description: Adds transaction_limit column for per-transaction limit enforcement (FR-001)

ALTER TABLE users ADD COLUMN transaction_limit DECIMAL(19,4) DEFAULT 1000.0000 NOT NULL;

-- Add index for potential future queries filtering by limit
CREATE INDEX idx_users_transaction_limit ON users(transaction_limit);

-- Update any existing users to have the default limit
UPDATE users SET transaction_limit = 1000.0000 WHERE transaction_limit IS NULL;
