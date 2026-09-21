package com.casualgames.websockethub.provider;

import com.casualgames.websockethub.domain.entity.WsTicketData;
import org.springframework.http.server.ServerHttpRequest;

public interface IdentityProvider {

    WsTicketData resolveTicket(ServerHttpRequest request);
}
