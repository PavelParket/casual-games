package com.notifications.repository;

import com.notifications.domain.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    long countByIdGreaterThan(long id);

    @Query(value = """
            SELECT MAX(id) FROM announcements
            """, nativeQuery = true)
    Optional<Long> findMaxId();
}
