package com.casualgames.securityservice.service;

import com.casualgames.securityservice.config.properties.WsTicketProperties;
import com.casualgames.securityservice.domain.entity.WsTicketData;
import com.casualgames.securityservice.repository.redis.WsTicketRedisRepository;
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
