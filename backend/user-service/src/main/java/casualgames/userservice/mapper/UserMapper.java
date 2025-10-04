package casualgames.userservice.mapper;

import casualgames.userservice.dto.UserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserRequest dto);

    UserResponse toResponseDto(User user);

    List<UserResponse> toListResponse(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toUpdateEntity(UserRequest dto, @MappingTarget User user);
}
