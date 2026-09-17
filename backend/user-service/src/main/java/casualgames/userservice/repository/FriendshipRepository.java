package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
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

    @Query(value = """
            SELECT *
            FROM friendship
            WHERE (user_guid = :userGuid AND friend_guid IN :userGuids)
            OR (friend_guid = :userGuid AND user_guid IN :userGuids)
            """, nativeQuery = true)
    List<Friendship> findByUserGuidAndUserGuidIn(UUID userGuid, Collection<UUID> userGuids);

    @Query(value = """
            SELECT *
            FROM friendship
            WHERE user_guid = :userGuid
            OR friend_guid = :userGuid
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM friendship
                    WHERE user_guid = :userGuid
                    OR friend_guid = :userGuid
                    """,
            nativeQuery = true)
    Page<Friendship> findByUserGuid(UUID userGuid, Pageable pageable);
}
