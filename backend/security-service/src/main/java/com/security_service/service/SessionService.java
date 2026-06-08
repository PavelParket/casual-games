package com.security_service.service;

import com.security_service.domain.entity.SessionData;
import com.security_service.exception.SessionRevokedException;
import com.security_service.repository.redis.SessionRedisRepository;
import com.security_service.service.helper.RefreshHashHelper;
import com.security_starter.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_SESSION;
import static com.security_service.config.ResourceMessageConstants.REFRESH_TOKEN_REUSE;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRedisRepository sessionRedisRepository;

    private final RefreshHashHelper refreshHasher;

    private final JwtProperties jwtProperties;

    public void createSession(UUID guid, UUID sid, String refreshToken) {
        String refreshHash = refreshHasher.hash(refreshToken);
        long now = System.currentTimeMillis();
        Duration ttl = Duration.ofMillis(jwtProperties.refreshExpiration());

        SessionData data = new SessionData(refreshHash, null, now, now);

        sessionRedisRepository.save(guid, sid, data, ttl);
        sessionRedisRepository.indexAdd(guid, sid);

        log.debug("Session created: guid={}, sid={}", guid, sid);
    }

    public void rotate(UUID guid, UUID sid, String oldRefreshToken, String newRefreshToken) {
        SessionData session = sessionRedisRepository.find(guid, sid)
                .orElseThrow(() -> new SessionRevokedException(NOT_FOUND_SESSION));

        String incomingHash = refreshHasher.hash(oldRefreshToken);

        if (!incomingHash.equals(session.getRefreshHash())) {
            sessionRedisRepository.delete(guid, sid);
            sessionRedisRepository.indexRemove(guid, sid);

            log.warn("Refresh token reuse detected: guid={}, sid={}", guid, sid);

            throw new SessionRevokedException(REFRESH_TOKEN_REUSE);
        }

        String newHash = refreshHasher.hash(newRefreshToken);
        long now = System.currentTimeMillis();
        Duration ttl = Duration.ofMillis(jwtProperties.refreshExpiration());

        SessionData updated = SessionData.builder()
                .refreshHash(newHash)
                .deviceLabel(session.getDeviceLabel())
                .createdAt(session.getCreatedAt())
                .lastUsedAt(now)
                .build();

        sessionRedisRepository.save(guid, sid, updated, ttl);

        log.debug("Session rotated: guid={}, sid={}", guid, sid);
    }

    public void revoke(UUID guid, UUID sid) {
        sessionRedisRepository.delete(guid, sid);
        sessionRedisRepository.indexRemove(guid, sid);

        log.debug("Session revoked: guid={}, sid={}", guid, sid);
    }

    public void revokeAll(UUID guid) {
        Set<String> sids = sessionRedisRepository.indexGetAll(guid);

        for (String sid : sids) {
            try {
                sessionRedisRepository.delete(guid, UUID.fromString(sid));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sid in session index: guid={}, sid={}", guid, sid);
            }
        }

        sessionRedisRepository.indexClear(guid);

        log.debug("All sessions revoked: guid={}, count={}", guid, sids.size());
    }
}
