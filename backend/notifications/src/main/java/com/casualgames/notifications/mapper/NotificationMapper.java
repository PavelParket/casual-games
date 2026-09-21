package com.casualgames.notifications.mapper;

import com.casualgames.commonutils.mapper.PagedModelMapper;
import com.casualgames.kafkastarter.dto.event.NotificationEvent;
import com.casualgames.notifications.domain.dto.NotificationResponse;
import com.casualgames.notifications.domain.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface NotificationMapper extends PagedModelMapper {

    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    Notification toEntity(NotificationEvent event, String title, String body, String link);

    NotificationResponse toResponse(Notification notification);
}
