package com.game_service.mahjong.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Slot {

    private String id;

    private int col;

    private int row;

    private int layer;

    private List<String> covers;

    private String leftId;

    private String rightId;
}
