package com.game_service.mahjong.domain.dto;

import lombok.Builder;

@Builder
public record MahjongTileResponse(

        String slotId,

        String suit,

        int value
) {
}
