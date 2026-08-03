package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Long countByRequesterGuidAndStatusAndCreatedAtAfter(UUID requesterGuid,
                                                        FriendRequestStatus status,
                                                        Instant date);

    @Query(value = """
            SELECT *
            FROM friend_request
            WHERE requester_guid = :requesterGuid
            AND recipient_guid = :recipientGuid
            AND status IN :statuses
            ORDER BY created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<FriendRequest> findLatestByRequesterGuidAndRecipientGuidAndStatusIn(UUID requesterGuid,
                                                                                 UUID recipientGuid,
                                                                                 Collection<String> statuses);

    @Query(value = """
            SELECT *
            FROM friend_request
            WHERE (
                (requester_guid = :requesterGuid AND recipient_guid IN :recipientGuids)
                OR (recipient_guid = :requesterGuid AND requester_guid IN :recipientGuids)
            )
            AND status IN :statuses
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<FriendRequest> findLatestByRequesterGuidAndRecipientGuidInAndStatusIn(UUID requesterGuid,
                                                                               Collection<UUID> recipientGuids,
                                                                               Collection<String> statuses);

    Page<FriendRequest> findAllByRequesterGuidAndStatus(UUID requesterGuid,
                                                        FriendRequestStatus status,
                                                        Pageable pageable);

    Page<FriendRequest> findAllByRecipientGuidAndStatus(UUID recipientGuid,
                                                        FriendRequestStatus status,
                                                        Pageable pageable);
}
