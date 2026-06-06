package com.websocket_hub.domain.entity;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class WsTicketData {

    UUID userGuid;

    UUID tokenSid;

    UUID roomId;
}
