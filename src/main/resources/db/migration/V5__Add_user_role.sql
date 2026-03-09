-- V5: Add role column to users table for RBAC support
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER';
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'));
