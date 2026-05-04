package com.consignment.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private final int maxAttempts;
    private final int windowSeconds;
    private final int blockSeconds;

    private final ConcurrentHashMap<String, AttemptState> states = new ConcurrentHashMap<>();

    public LoginRateLimiter(
            @Value("${auth.login-rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${auth.login-rate-limit.window-seconds:300}") int windowSeconds,
            @Value("${auth.login-rate-limit.block-seconds:900}") int blockSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.blockSeconds = blockSeconds;
    }

    public OptionalLong checkBlocked(String key) {
        AttemptState state = states.get(key);
        if (state == null) {
            return OptionalLong.empty();
        }

        Instant now = Instant.now();
        synchronized (state) {
            if (state.blockedUntil != null && now.isBefore(state.blockedUntil)) {
                return OptionalLong.of(Math.max(1, state.blockedUntil.getEpochSecond() - now.getEpochSecond()));
            }
            if (state.blockedUntil != null && !now.isBefore(state.blockedUntil)) {
                state.blockedUntil = null;
                state.failures.clear();
            }
        }

        return OptionalLong.empty();
    }

    public OptionalLong registerFailure(String key) {
        AttemptState state = states.computeIfAbsent(key, ignored -> new AttemptState());
        Instant now = Instant.now();

        synchronized (state) {
            pruneExpiredFailures(state, now);
            state.failures.addLast(now);

            if (state.failures.size() >= maxAttempts) {
                state.blockedUntil = now.plusSeconds(blockSeconds);
                state.failures.clear();
                return OptionalLong.of(blockSeconds);
            }
        }

        return OptionalLong.empty();
    }

    public void clearFailures(String key) {
        states.remove(key);
    }

    private void pruneExpiredFailures(AttemptState state, Instant now) {
        Instant floor = now.minusSeconds(windowSeconds);
        while (!state.failures.isEmpty() && state.failures.peekFirst().isBefore(floor)) {
            state.failures.pollFirst();
        }
    }

    private static final class AttemptState {
        private final Deque<Instant> failures = new ArrayDeque<>();
        private Instant blockedUntil;
    }
}
