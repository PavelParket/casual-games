package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RoomRequest(

        @NotBlank(message = "Room name must not be blank")
        @Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
        String roomName,

        @NotNull(message = "Room type must not be null")
        RoomType roomType
) {
}
