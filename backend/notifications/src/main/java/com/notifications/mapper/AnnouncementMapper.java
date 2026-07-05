package com.notifications.mapper;

import com.notifications.domain.dto.AnnouncementCreateRequest;
import com.notifications.domain.dto.AnnouncementResponse;
import com.notifications.domain.entity.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface AnnouncementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    Announcement toEntity(AnnouncementCreateRequest request, UUID createdBy);

    AnnouncementResponse toResponse(Announcement announcement);

    default Page<AnnouncementResponse> toResponsePage(Page<Announcement> announcements) {
        return announcements.map(this::toResponse);
    }
}
