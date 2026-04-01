package com.websocket_hub.domain.entity;

import lombok.Builder;

@Builder
public record DurakTablePair(

        DurakCard attackCard,

        DurakCard defendCard
) {
}
