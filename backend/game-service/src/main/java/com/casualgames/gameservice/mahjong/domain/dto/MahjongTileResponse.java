package com.casualgames.gameservice.mahjong.domain.dto;

import lombok.Builder;

@Builder
public record MahjongTileResponse(

        String slotId,

        String suit,

        int value
) {
}
