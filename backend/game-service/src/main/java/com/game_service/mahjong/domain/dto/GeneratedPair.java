package com.game_service.mahjong.domain.dto;

import com.game_service.mahjong.domain.entity.TileFace;
import lombok.Builder;

@Builder
public record GeneratedPair(

        String slot1,

        String slot2,

        TileFace face
) {
}
