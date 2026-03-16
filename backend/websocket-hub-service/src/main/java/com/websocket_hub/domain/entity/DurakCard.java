package com.websocket_hub.domain.entity;

import com.websocket_hub.domain.enums.model.DurakCardRank;
import com.websocket_hub.domain.enums.model.DurakCardSuit;
import lombok.Builder;

@Builder
public record DurakCard(

        DurakCardRank rank,

        DurakCardSuit suit
) {
}
