package casualgames.userservice.domain.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record UserFriendResponseList(

        PagedModel<UserFriendResponse> users
) {
}
