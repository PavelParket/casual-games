package com.security_service.service;

import com.security_service.config.properties.WsTicketProperties;
import com.security_service.domain.entity.WsTicketData;
import com.security_service.repository.redis.WsTicketRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WsTicketService {

    private final WsTicketRedisRepository wsTicketRedisRepository;

    private final WsTicketProperties wsTicketProperties;

    public String create(UUID guid, UUID sid, UUID roomId) {
        UUID ticketId = UUID.randomUUID();

        WsTicketData data = WsTicketData.builder()
                .userGuid(guid)
                .tokenSid(sid)
                .roomId(roomId)
                .build();

        wsTicketRedisRepository.save(ticketId, data, wsTicketProperties.ttlSeconds());

        return ticketId.toString();
    }
}
