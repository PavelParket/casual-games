package com.websocket_hub.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HorseRaceHorseKeyframes {

    private Integer horseIndex;

    private List<HorseRaceKeyframe> keyframes;
}
