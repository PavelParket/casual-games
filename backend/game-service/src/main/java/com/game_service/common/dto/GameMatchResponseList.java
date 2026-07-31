package com.game_service.common.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record GameMatchResponseList(

        PagedModel<GameMatchResponse> gameMatches
) {
}
