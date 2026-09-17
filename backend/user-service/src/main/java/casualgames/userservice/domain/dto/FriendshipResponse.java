package casualgames.userservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendshipResponse {

    private UserResponse user;

    private UserResponse friend;

    private Instant friendshipDate;
}
