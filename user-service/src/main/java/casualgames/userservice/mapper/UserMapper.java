package casualgames.userservice.mapper;

import casualgames.userservice.dto.UserRequestDTO;
import casualgames.userservice.dto.UserResponseDTO;
import casualgames.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserRequestDTO dto);

    @Mapping(target = "password", ignore = true)
    UserResponseDTO toResponseDto(User user);

}
