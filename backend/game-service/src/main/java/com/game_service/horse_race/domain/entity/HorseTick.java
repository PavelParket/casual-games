package com.game_service.horse_race.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record HorseTick(

        @JsonProperty("tickIndex")
        int tickIndex,

        @JsonProperty("positions")
        double[] positions
) {
}
