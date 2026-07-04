package com.notifications.mapper;

import com.kafka_starter.dto.event.NotificationEvent;
import com.notifications.domain.dto.NotificationResponse;
import com.notifications.domain.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface NotificationMapper {

    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    Notification toEntity(NotificationEvent event, String title, String body, String link);

    NotificationResponse toResponse(Notification notification);

    default Page<NotificationResponse> toResponsePage(Page<Notification> notifications) {
        return notifications.map(this::toResponse);
    }
}
