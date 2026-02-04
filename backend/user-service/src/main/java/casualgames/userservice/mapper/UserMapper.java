package casualgames.userservice.mapper;

import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.dto.security_service.UpdateUserInternalRequest;
import casualgames.userservice.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);

    UserResponse toResponseDto(User user);

    List<UserResponse> toListResponse(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UpdateUserRequest updateUserRequest, @MappingTarget User user);

    UpdateUserInternalRequest toUpdateUserInternalRequest(User user, String password);

    UserResponseDto toDto(User user);
}
