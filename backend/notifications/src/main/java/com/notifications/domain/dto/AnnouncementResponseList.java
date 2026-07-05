package com.notifications.domain.dto;

import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record AnnouncementResponseList(

        Page<AnnouncementResponse> announcements,

        long unread
) {
}
