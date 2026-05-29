package casualgames.userservice.mapper;

import casualgames.userservice.domain.dto.UserResponse;
import casualgames.userservice.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toListResponse(List<User> users);
}
