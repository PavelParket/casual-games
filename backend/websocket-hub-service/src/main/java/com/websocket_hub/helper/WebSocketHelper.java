package com.websocket_hub.helper;

import com.websocket_hub.domain.context.WebSocketContext;
import com.websocket_hub.domain.dto.message.Message;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.EventType;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.mapper.MessageMapper;
import com.websocket_hub.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketHelper {

    public static final String BET_ACCEPTED_MESSAGE = "Your bet has been accepted: %s";
    public static final String BET_ACCEPTED_OTHER_MESSAGE = "Player %s made a bet: %s";
    public static final String BET_REJECTED_MESSAGE = "Bet rejected: %s";
    public static final String BET_REQUIRED_MESSAGE = "You must place a bet before becoming ready";
    public static final String BET_OUTBID_MESSAGE = "Your bet has been outbid by: %s, please make a new one";

    @Qualifier("messageMapperImpl")
    private final MessageMapper messageMapper;

    private final SessionManager sessionManager;

    public void notifyBetAccepted(UUID roomId, ClientSession client, EventType event, BigDecimal amount) {
        sendToPlayer(roomId, client, event, String.format(BET_ACCEPTED_MESSAGE, amount));
    }

    public void notifyBetRejected(UUID roomId, ClientSession client, EventType event, String reason) {
        sendToPlayer(roomId, client, event, String.format(BET_REJECTED_MESSAGE, reason));
    }

    public void notifyBetRequired(UUID roomId, ClientSession client, EventType event) {
        sendToPlayer(roomId, client, event, BET_REQUIRED_MESSAGE);
    }

    public void notifyBetOutbid(UUID roomId, ClientSession client, EventType event, BigDecimal newAmount) {
        sendToPlayer(roomId, client, event, String.format(BET_OUTBID_MESSAGE, newAmount));
    }

    public void notifyBetAcceptedToAll(UUID roomId, ClientSession client, Set<ClientSession> others, EventType event, BigDecimal amount) {
        sendToPlayer(roomId, client, event, String.format(BET_ACCEPTED_MESSAGE, amount));

        if (others == null || others.isEmpty()) {
            return;
        }

        others.forEach(other -> {
            if (other != null && !other.getGuid().equals(client.getGuid())) {
                sessionManager.sendToSession(other, messageMapper.toResponse(
                        MessageType.SYSTEM,
                        event,
                        client.getGuid(),
                        other.getGuid(),
                        roomId,
                        String.format(BET_ACCEPTED_OTHER_MESSAGE, client.getUsername(), amount)
                ));
            }
        });
    }

    private void sendToPlayer(UUID roomId, ClientSession client, EventType event, String message) {
        if (client == null) {
            log.warn("Cannot send notification — client is null: event={}, roomId={}", event, roomId);
            return;
        }

        sessionManager.sendToSession(client, messageMapper.toResponse(
                MessageType.SYSTEM,
                event,
                client.getGuid(),
                client.getGuid(),
                roomId,
                message
        ));
    }

    public WebSocketContext getWebsocketContext(WebSocketSession session) {
        return WebSocketContext.builder()
                .user(WebSocketUtil.getUser(session))
                .roomId(WebSocketUtil.getRoomId(session))
                .session(session)
                .connectedAt(WebSocketUtil.getConnectedAt(session))
                .build();
    }

    public void sendToSession(UUID playerGuid, Message<? extends EventType> message) {
        ClientSession client = sessionManager.getByGuid(playerGuid);

        if (client == null) {
            log.warn("Cannot send message — session not found: playerGuid={}", playerGuid);
            return;
        }

        sessionManager.sendToSession(client, message);
    }

    public void broadcastToSessions(Set<ClientSession> clients, Message<? extends EventType> message) {
        if (clients == null || clients.isEmpty()) {
            return;
        }

        List<Thread> threads = new ArrayList<>();

        for (ClientSession client : clients) {
            if (client == null) {
                continue;
            }

            Thread thread = Thread.ofVirtual().start(() -> sessionManager.sendToSession(client, message));

            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted during broadcast to sessions");
            }
        }
    }
}
