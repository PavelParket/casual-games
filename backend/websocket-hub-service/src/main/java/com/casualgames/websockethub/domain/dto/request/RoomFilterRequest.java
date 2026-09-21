package com.casualgames.websockethub.domain.dto.request;

import com.casualgames.websockethub.domain.enums.RoomSortField;
import com.casualgames.websockethub.domain.enums.RoomType;
import com.casualgames.websockethub.domain.enums.SortDirection;
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
