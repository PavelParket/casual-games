package com.casualgames.notifications.mapper;

import com.casualgames.commonutils.mapper.PagedModelMapper;
import com.casualgames.notifications.domain.dto.AnnouncementCreateRequest;
import com.casualgames.notifications.domain.dto.AnnouncementResponse;
import com.casualgames.notifications.domain.entity.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface AnnouncementMapper extends PagedModelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    Announcement toEntity(AnnouncementCreateRequest request, UUID createdBy);

    AnnouncementResponse toResponse(Announcement announcement);
}
