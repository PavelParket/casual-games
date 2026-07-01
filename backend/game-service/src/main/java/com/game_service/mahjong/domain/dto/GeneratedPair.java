package com.game_service.mahjong.domain.dto;

import com.game_service.mahjong.domain.entity.TileFace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneratedPair {

    private String slot1;

    private String slot2;

    private TileFace face;
}
