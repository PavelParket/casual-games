package com.game_service.mahjong.domain.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MahjongBoardResponse(

        UUID playerGuid,

        List<MahjongTileResponse> tiles
) {
}
