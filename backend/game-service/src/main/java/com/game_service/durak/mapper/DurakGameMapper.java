package com.game_service.durak.mapper;

import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.domain.entity.Durak;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DurakGameMapper {

    DurakGameResponse toResponse(Durak game);
}
