package casualgames.userservice.mapper;

import casualgames.userservice.domain.dto.FriendResponse;
import casualgames.userservice.domain.dto.UserResponse;
import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.enums.RelationshipStatus;
import com.common_utils.mapper.PagedModelMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FriendMapper extends PagedModelMapper<Friendship, FriendResponse> {

    @Mapping(target = "friendshipDate", source = "friendship.createdAt")
    FriendResponse toResponse(Friendship friendship, UserResponse friend, RelationshipStatus relationshipStatus);
}
