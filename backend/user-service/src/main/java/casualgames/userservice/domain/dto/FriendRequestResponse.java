package casualgames.userservice.domain.dto;

import casualgames.userservice.domain.enums.FriendRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestResponse {

    private Long id;

    private UserResponse requester;

    private UserResponse recipient;

    private FriendRequestStatus status;

    private Instant createdAt;

    private Instant resolvedAt;
}
