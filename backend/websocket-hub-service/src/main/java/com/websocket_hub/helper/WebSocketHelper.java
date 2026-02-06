package com.websocket_hub.helper;

import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketHelper {

    @Qualifier("messageMapperImpl")
    private final MessageMapper messageMapper;

    private final SessionManager sessionManager;

    public void notifyBetAccepted(UUID roomId, ClientSession client, Set<ClientSession> clients, BigDecimal bet) {
        if (client == null) {
            log.warn("Cannot notify bet accepted - client is null");
            return;
        }

        sessionManager.sendToSession(client, messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.BET,
                client.getGuid(),
                client.getGuid(),
                roomId,
                "Your bet has been accepted: " + bet
        ));

        if (clients != null && !clients.isEmpty()) {
            clients.forEach(otherClient -> {
                if (otherClient != null && !otherClient.getGuid().equals(client.getGuid())) {
                    sessionManager.sendToSession(otherClient, messageMapper.toResponse(
                            MessageType.SYSTEM,
                            TicTacToeGameEvent.BET,
                            client.getGuid(),
                            otherClient.getGuid(),
                            roomId,
                            "Player " + client.getUsername() + " made the bet: " + bet
                    ));
                }
            });
        }
    }

    public void notifyBetRejected(UUID roomId, ClientSession client, BigDecimal bet) {
        if (client == null) {
            log.warn("Cannot notify bet rejected - client is null");
            return;
        }

        sessionManager.sendToSession(client, messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.BET_REJECT,
                client.getGuid(),
                client.getGuid(),
                roomId,
                "Your bet has been rejected: " + bet
        ));
    }

    public void notifyOutbid(UUID roomId, ClientSession client, BigDecimal bet) {
        if (client == null) {
            log.warn("Cannot notify outbid - client is null");
            return;
        }

        sessionManager.sendToSession(client, messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.BET_OUTBID,
                client.getGuid(),
                client.getGuid(),
                roomId,
                "Your bet has been outbid by: " + bet + ", please, make new"
        ));
    }

    public void notifyBetRequired(UUID roomId, ClientSession client) {
        if (client == null) {
            log.warn("Cannot notify bet required - client is null");
            return;
        }

        sessionManager.sendToSession(client, messageMapper.toResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.BET_REQUIRED,
                client.getGuid(),
                client.getGuid(),
                roomId,
                "You must place a bet before becoming ready"
        ));
    }
}
