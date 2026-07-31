package com.notifications.repository;

import com.notifications.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByEventId(UUID eventId);

    Page<Notification> findByRecipientGuid(UUID recipientGuid, Pageable pageable);

    long countByRecipientGuidAndReadAtIsNull(UUID recipientGuid);

    @Modifying
    @Query(value = """
            UPDATE notifications
            SET read_at = :now
            WHERE recipient_guid = :recipientGuid
            AND read_at IS NULL
            """, nativeQuery = true)
    void markAllAsRead(UUID recipientGuid, Instant now);

    @Modifying
    @Query(value = """
            DELETE FROM notifications
            WHERE read_at IS NOT NULL
            AND created_at < :date
            """, nativeQuery = true)
    void deleteRead(Instant date);
}
