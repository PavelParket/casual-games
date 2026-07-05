package com.notifications.repository;

import com.notifications.domain.entity.UserAnnouncementState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAnnouncementStateRepository extends JpaRepository<UserAnnouncementState, UUID> {
}
