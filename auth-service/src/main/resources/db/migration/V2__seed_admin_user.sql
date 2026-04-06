-- BCrypt hash of 'secret123' (generated with pgcrypto gen_salt('bf', 10))
INSERT INTO users (username, password, email, enabled)
VALUES ('admin', '$2a$10$ex3DGL0VGZhXyyQp1xgd8e2DmwfmwGpZkLenSQR14fDmq8L4PG7GG', 'admin@consignment.com', true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;
