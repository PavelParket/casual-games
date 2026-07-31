package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.FriendRequest;
import casualgames.userservice.domain.enums.FriendRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findByRequesterGuidAndRecipientGuidAndStatus(UUID requesterGuid, UUID recipientGuid, FriendRequestStatus status);

    long deleteByRequesterGuidAndRecipientGuidAndStatus(UUID requesterGuid, UUID recipientGuid, FriendRequestStatus status);

    long countByRequesterGuidAndStatusAndCreatedAtAfter(UUID requesterGuid, FriendRequestStatus status, Instant liveSince);

    //long countByRecipientGuidAndStatusAndCreatedAtAfter(UUID recipientGuid, FriendRequestStatus status, Instant liveSince);

    Page<FriendRequest> findAllByRecipientGuidAndStatusAndCreatedAtAfter(UUID recipientGuid,
                                                                         FriendRequestStatus status,
                                                                         Instant liveSince,
                                                                         Pageable pageable);

    Page<FriendRequest> findAllByRequesterGuidAndStatusAndCreatedAtAfter(UUID requesterGuid,
                                                                         FriendRequestStatus status,
                                                                         Instant liveSince,
                                                                         Pageable pageable);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM FriendRequest r
            WHERE r.status IN ('DECLINED', 'WITHDRAWN')
            AND r.resolvedAt > :since
            AND ((r.fromGuid = :requesterGuid AND r.toGuid = :recipientGuid)
                OR (r.fromGuid = :recipientGuid AND r.toGuid = :requesterGuid))
            """)
    boolean existsCooldown(UUID requesterGuid, UUID recipientGuid, Instant since);
}
