package casualgames.userservice.domain.dto;

import casualgames.userservice.domain.enums.RelationshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendResponse {

    private UserResponse friend;

    private RelationshipStatus relationshipStatus;

    private Instant friendshipDate;
}
