-- Seed consignee_user with password 'password123'
-- BCrypt hash generated with cost 10
INSERT INTO users (username, password, email, enabled)
VALUES (
    'consignee_user',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'consignee@store-a.com',
    true
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_CONSIGNEE' FROM users WHERE username = 'consignee_user'
ON CONFLICT DO NOTHING;

INSERT INTO user_profiles (user_id, consignee_store)
SELECT id, 'STORE_A' FROM users WHERE username = 'consignee_user'
ON CONFLICT (user_id) DO NOTHING;
