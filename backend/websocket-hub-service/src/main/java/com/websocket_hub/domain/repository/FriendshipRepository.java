package com.websocket_hub.domain.repository;

import com.websocket_hub.domain.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query(value = """
            SELECT count(*)
            FROM friendship
            WHERE (user_guid = :userGuid AND friend_guid = :friendGuid)
            OR (user_guid = :friendGuid AND friend_guid = :userGuid)
            """, nativeQuery = true)
    boolean existsByUserGuidAndFriendGuidOrUserGuidAndFriendGuid(UUID userGuid, UUID friendGuid);

    @Query(value = """
            SELECT *
            FROM friendship
            WHERE (user_guid = :userGuid AND friend_guid IN :userGuids)
            OR (friend_guid = :userGuid AND user_guid IN :userGuids)
            """, nativeQuery = true)
    List<Friendship> findByUserGuidAndUserGuidIn(UUID userGuid, Collection<UUID> userGuids);

    @Query(value = """
            SELECT CASE
                WHEN user_guid = :userGuid
                    THEN friend_guid
                    ELSE user_guid
                END
            FROM friendship
            WHERE user_guid = :userGuid
            OR friend_guid = :userGuid
            """, nativeQuery = true)
    List<UUID> findFriendGuidsByUserGuid(UUID userGuid);
}
