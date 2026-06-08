package com.security_service.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis_starter.repository.RedisRepository;
import com.security_service.domain.entity.WsTicketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WsTicketRedisRepository {

    private static final String TICKET_PREFIX = "ws_ticket";
    private static final String TICKET_KEY_FORMAT = "%s:%s";

    private final RedisRepository redisRepository;

    private final ObjectMapper objectMapper;

    public void save(UUID ticketId, WsTicketData data, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisRepository.set(buildTicketKey(ticketId), json, Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize WsTicketData: ticketId={}", ticketId, e);
        }
    }

    private String buildTicketKey(UUID ticketId) {
        return String.format(TICKET_KEY_FORMAT, TICKET_PREFIX, ticketId);
    }
}
