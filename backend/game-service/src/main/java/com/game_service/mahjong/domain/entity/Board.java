package com.game_service.mahjong.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board {

    Map<String, TileFace> faces;

    Set<String> removed;

    Set<String> freeSet;

    int remaining;
}
