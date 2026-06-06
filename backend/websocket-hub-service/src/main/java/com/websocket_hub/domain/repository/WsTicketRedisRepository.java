package com.websocket_hub.domain.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis_starter.repository.RedisRepository;
import com.websocket_hub.domain.entity.WsTicketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WsTicketRedisRepository {

    private static final String TICKET_KEY_FORMAT = "ws_ticket:%s";
    private static final String SESSION_KEY_FORMAT = "session:%s:%s";

    private final RedisRepository redisRepository;

    private final ObjectMapper objectMapper;

    public Optional<WsTicketData> getTicket(UUID ticketId) {
        String key = String.format(TICKET_KEY_FORMAT, ticketId);
        String json = redisRepository.getAndDelete(key);

        if (json == null) {
            log.warn("WS ticket not found or expired: ticketId={}", ticketId);
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(json, WsTicketData.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize WsTicketData: ticketId={}", ticketId, e);
            return Optional.empty();
        }
    }

    public boolean isSessionActive(UUID userGuid, UUID tokenSid) {
        String key = String.format(SESSION_KEY_FORMAT, userGuid, tokenSid);

        return redisRepository.exists(key);
    }
}
