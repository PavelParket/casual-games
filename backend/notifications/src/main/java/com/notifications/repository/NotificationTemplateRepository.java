package com.notifications.repository;

import com.notifications.domain.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    @Query(value = """
            SELECT * FROM notification_templates
            WHERE type = :type
            AND enabled = true
            """, nativeQuery = true)
    Optional<NotificationTemplate> findByType(String type);
}
