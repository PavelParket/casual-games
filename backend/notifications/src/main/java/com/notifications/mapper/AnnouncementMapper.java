package com.notifications.mapper;

import com.common_utils.mapper.PagedModelMapper;
import com.notifications.domain.dto.AnnouncementCreateRequest;
import com.notifications.domain.dto.AnnouncementResponse;
import com.notifications.domain.entity.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface AnnouncementMapper extends PagedModelMapper<Announcement, AnnouncementResponse> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    Announcement toEntity(AnnouncementCreateRequest request, UUID createdBy);

    AnnouncementResponse toResponse(Announcement announcement);
}
