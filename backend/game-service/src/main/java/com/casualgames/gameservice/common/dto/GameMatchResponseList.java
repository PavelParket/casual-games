package com.casualgames.gameservice.common.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record GameMatchResponseList(

        PagedModel<GameMatchResponse> gameMatches
) {
}
