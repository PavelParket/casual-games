package casualgames.userservice.mapper;

import casualgames.userservice.domain.dto.FriendRequestResponse;
import casualgames.userservice.domain.dto.UserResponse;
import casualgames.userservice.domain.entity.FriendRequest;
import com.common_utils.mapper.PagedModelMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FriendRequestMapper extends PagedModelMapper<FriendRequest, FriendRequestResponse> {

    FriendRequestResponse toResponse(FriendRequest friendRequest, UserResponse requester, UserResponse recipient);
}
