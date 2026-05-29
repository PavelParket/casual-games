package casualgames.userservice.repository;

import casualgames.userservice.domain.entity.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findByUserGuid(UUID userGuid);

    @Query(value = """
            SELECT s.* FROM user_subscriptions s
            JOIN users u ON u.guid = s.user_guid
            WHERE u.status != 'DEFAULT'
              AND (
                  s.expires_at <= :now
                  OR (s.status_change_at IS NOT NULL AND s.status_change_at <= :now)
              )
            """, nativeQuery = true)
    Page<UserSubscription> findExpiringOrScheduled(Instant now, Pageable pageable);
}
