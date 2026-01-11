package com.websocket_hub.service;

import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerBetService {

    private final MessageMapper messageMapper;

    public void notifyBetAccepted(String roomName, ClientSession client, BigDecimal bet, AbstractRoomManager roomManager) {
        roomManager.sendToSession(client, messageMapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.BET, roomName, "Your bet has been accepted: " + bet));
    }

    public void notifyBetRejected(String roomName, ClientSession client, BigDecimal bet, AbstractRoomManager roomManager) {
        roomManager.sendToSession(client, messageMapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.BET_REJECT, roomName, "Your bet has been rejected: " + bet));
    }

    public void notifyOutbid(String roomName, ClientSession client, BigDecimal bet, AbstractRoomManager roomManager) {
        roomManager.sendToSession(client, messageMapper.toResponse(MessageType.SYSTEM, TicTacToeGameEvent.BET_OUTBID, roomName, "Your bet has been outbid by: " + bet + ", please, make new"));
    }
}
