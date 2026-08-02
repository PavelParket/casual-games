package casualgames.userservice.mapper;

import casualgames.userservice.domain.dto.FriendshipResponse;
import casualgames.userservice.domain.dto.UserResponse;
import casualgames.userservice.domain.entity.Friendship;
import com.common_utils.mapper.PagedModelMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FriendshipMapper extends PagedModelMapper<Friendship, FriendshipResponse> {

    @Mapping(target = "friendshipDate", source = "friendship.createdAt")
    FriendshipResponse toResponse(Friendship friendship, UserResponse user, UserResponse friend);
}
