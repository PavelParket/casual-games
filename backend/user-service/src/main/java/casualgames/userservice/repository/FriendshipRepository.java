package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query(value = """
            SELECT *
            FROM friendship
            WHERE (user_guid = :userGuid AND friend_guid = :friendGuid)
            OR (user_guid = :friendGuid AND friend_guid = :userGuid)
            """, nativeQuery = true)
    Optional<Friendship> findByUserGuidAndFriendGuid(UUID userGuid, UUID friendGuid);
}
