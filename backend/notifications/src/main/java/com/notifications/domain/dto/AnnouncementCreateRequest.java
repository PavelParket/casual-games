package com.notifications.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AnnouncementCreateRequest(

        @NotBlank(message = "Title must not be empty")
        @Size(max = 255, message = "Title must not be longer than 255 characters")
        String title,

        @NotBlank(message = "Body must not be empty")
        String body
) {
}
