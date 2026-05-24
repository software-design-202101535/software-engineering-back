package com.example.edumanager.domain.oauth.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class OAuthPendingStore {

    private static final Duration TTL = Duration.ofSeconds(30);

    private final ConcurrentMap<String, Entry> store = new ConcurrentHashMap<>();

    public String save(OAuthPending pending) {
        String authCode = UUID.randomUUID().toString();
        store.put(authCode, new Entry(pending, Instant.now().plus(TTL)));
        return authCode;
    }

    public Optional<OAuthPending> consume(String authCode) {
        Entry entry = store.remove(authCode);
        if (entry == null || entry.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(entry.pending());
    }

    private record Entry(OAuthPending pending, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
