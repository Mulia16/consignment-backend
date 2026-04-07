CREATE TABLE IF NOT EXISTS token_blacklist (
    jti         VARCHAR(64)  PRIMARY KEY,
    expires_at  TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires ON token_blacklist(expires_at);
