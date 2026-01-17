package com.websocket_hub.service;

import com.websocket_hub.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerBetService {

    @Qualifier("messageMapperImpl")
    private final MessageMapper messageMapper;

    /*public void notifyBetAccepted(String roomName, ClientSession client, BigDecimal bet, SessionManager sessionManager) {
        sessionManager.sendToSession(client, messageMapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.BET, roomName, "Your bet has been accepted: " + bet));
    }

    public void notifyBetRejected(String roomName, ClientSession client, BigDecimal bet, SessionManager sessionManager) {
        sessionManager.sendToSession(client, messageMapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.BET_REJECT, roomName, "Your bet has been rejected: " + bet));
    }

    public void notifyOutbid(String roomName, ClientSession client, BigDecimal bet, SessionManager sessionManager) {
        sessionManager.sendToSession(client, messageMapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.BET_OUTBID, roomName, "Your bet has been outbid by: " + bet + ", please, make new"));
    }*/
}
