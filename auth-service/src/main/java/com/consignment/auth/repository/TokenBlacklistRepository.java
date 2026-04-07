package com.consignment.auth.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;

@Repository
public class TokenBlacklistRepository {

    private final JdbcTemplate jdbc;

    public TokenBlacklistRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void blacklist(String jti, Date expiresAt) {
        jdbc.update(
            "INSERT INTO token_blacklist (jti, expires_at) VALUES (?, ?) ON CONFLICT DO NOTHING",
            jti, new Timestamp(expiresAt.getTime())
        );
    }

    public boolean isBlacklisted(String jti) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(1) FROM token_blacklist WHERE jti = ? AND expires_at > NOW()",
            Integer.class, jti
        );
        return count != null && count > 0;
    }

    // cleanup expired tokens every hour
    @Scheduled(fixedDelay = 3_600_000)
    public void purgeExpired() {
        jdbc.update("DELETE FROM token_blacklist WHERE expires_at <= NOW()");
    }
}
