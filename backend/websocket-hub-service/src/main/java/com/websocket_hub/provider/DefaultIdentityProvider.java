package com.websocket_hub.provider;

import com.common_utils.exception.JwtException;
import com.websocket_hub.domain.entity.WsTicketData;
import com.websocket_hub.domain.repository.WsTicketRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static com.websocket_hub.config.ResourceMessageConstants.AUTHENTICATION_FAILED;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultIdentityProvider implements IdentityProvider {

    private static final String TICKET_PARAM = "ticket";
    private static final String ROOM_ID_PARAM = "roomId";

    private final WsTicketRedisRepository wsTicketRedisRepository;

    @Override
    public WsTicketData resolveTicket(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String ticketIdParam = params.getFirst(TICKET_PARAM);

        if (ticketIdParam == null || ticketIdParam.isBlank()) {
            throw new JwtException(AUTHENTICATION_FAILED);
        }

        try {
            UUID ticketId = UUID.fromString(ticketIdParam);
            UUID roomId = resolveRoomId(params);

            WsTicketData ticket = wsTicketRedisRepository.getTicket(ticketId)
                    .orElseThrow(() -> new JwtException(AUTHENTICATION_FAILED));

            if (!ticket.getRoomId().equals(roomId)) {
                log.warn("WS ticket roomId mismatch: ticketRoomId={}, urlRoomId={}", ticket.getRoomId(), roomId);
                throw new JwtException(AUTHENTICATION_FAILED);
            }

            if (!wsTicketRedisRepository.isSessionActive(ticket.getUserGuid(), ticket.getTokenSid())) {
                log.warn("WS ticket rejected — session not active: userGuid={}", ticket.getUserGuid());
                throw new JwtException(AUTHENTICATION_FAILED);
            }

            return ticket;
        } catch (IllegalArgumentException e) {
            throw new JwtException(AUTHENTICATION_FAILED);
        }
    }

    private UUID resolveRoomId(MultiValueMap<String, String> params) {
        String roomId = params.getFirst(ROOM_ID_PARAM);

        if (roomId == null || roomId.isBlank()) {
            throw new JwtException(AUTHENTICATION_FAILED);
        }

        return UUID.fromString(roomId);
    }
}
