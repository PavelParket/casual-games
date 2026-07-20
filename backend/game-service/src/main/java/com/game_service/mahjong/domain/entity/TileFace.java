package com.game_service.mahjong.domain.entity;

import com.game_service.mahjong.domain.enums.TileSuit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TileFace {

    private TileSuit suit;

    private int value;
}
