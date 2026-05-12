package com.websocket_hub.domain.dto.request;

import com.websocket_hub.domain.enums.RoomSortField;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.domain.enums.SortDirection;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record RoomFilterRequest(

        String name,

        Set<RoomType> types,

        @NotNull
        RoomSortField sortField,

        @NotNull
        SortDirection sortDirection
) {
}
