package com.websocket_hub.domain.entity;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record HorseRaceGamePreset(

        UUID roomId,

        Integer horseCount,

        List<Double> odds
) {
}
