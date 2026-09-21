package com.casualgames.websockethub.domain.dto.response;

import com.casualgames.websockethub.domain.enums.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record RoomResponseMap(

        Map<RoomType, List<RoomResponse>> rooms
) {
}
