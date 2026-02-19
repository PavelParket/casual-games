package com.websocket_hub.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record HorseRaceGameTick(

        @JsonProperty("tickIndex")
        Integer tickIndex,

        @JsonProperty("positions")
        List<Double> positions
) {
}
