package com.websocket_hub.domain.dto.response;

import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record RoomResponseMap(

        Map<RoomType, List<RoomResponse>> rooms
) {
}
