package com.notifications.domain.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record NotificationResponseList(

        PagedModel<NotificationResponse> notifications,

        long unread
) {
}
