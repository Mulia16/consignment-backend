-- V4__user_profiles.sql
-- Creates the user_profiles table for mapping consignee users to their assigned store.
-- This supports the ROLE_CONSIGNEE feature: each consignee user must have exactly one
-- entry here (one-to-one with users.id) before they can log in.

CREATE TABLE IF NOT EXISTS user_profiles (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL UNIQUE,
    consignee_store  VARCHAR(50)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles (user_id);

-- NOTE: ROLE_CONSIGNEE is a valid role value for the user_roles table.
-- The user_roles table stores per-user role assignments (user_id, role) — there is no
-- separate roles lookup table. ROLE_CONSIGNEE follows the same naming convention as the
-- existing roles (ROLE_ADMIN, ROLE_USER).
--
-- To create a consignee user and assign the role + store mapping, run:
--
--   INSERT INTO users (username, password, email, enabled)
--   VALUES ('consignee_user', '<bcrypt_hash>', 'user@store.com', true);
--
--   INSERT INTO user_roles (user_id, role)
--   SELECT id, 'ROLE_CONSIGNEE' FROM users WHERE username = 'consignee_user'
--   ON CONFLICT DO NOTHING;
--
--   INSERT INTO user_profiles (user_id, consignee_store)
--   SELECT id, 'STORE_A' FROM users WHERE username = 'consignee_user'
--   ON CONFLICT (user_id) DO NOTHING;
