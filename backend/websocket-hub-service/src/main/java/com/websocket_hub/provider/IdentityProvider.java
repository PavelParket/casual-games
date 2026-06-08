package com.websocket_hub.provider;

import com.websocket_hub.domain.entity.WsTicketData;
import org.springframework.http.server.ServerHttpRequest;

public interface IdentityProvider {

    WsTicketData resolveTicket(ServerHttpRequest request);
}
