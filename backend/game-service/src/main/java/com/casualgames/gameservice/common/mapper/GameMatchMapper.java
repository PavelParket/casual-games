package com.casualgames.gameservice.common.mapper;

import com.casualgames.commonutils.mapper.PagedModelMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMatchMapper extends PagedModelMapper {
}
