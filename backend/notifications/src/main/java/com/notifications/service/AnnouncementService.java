package com.notifications.service;

import com.notifications.domain.dto.AnnouncementCreateRequest;
import com.notifications.domain.dto.AnnouncementResponse;
import com.notifications.domain.dto.AnnouncementResponseList;
import com.notifications.domain.entity.Announcement;
import com.notifications.domain.entity.UserAnnouncementState;
import com.notifications.mapper.AnnouncementMapper;
import com.notifications.repository.AnnouncementRepository;
import com.notifications.repository.UserAnnouncementStateRepository;
import com.security_starter.config.AuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private static final long ZERO_CURSOR = 0L;

    private final AnnouncementRepository announcementRepository;

    private final UserAnnouncementStateRepository userAnnouncementStateRepository;

    private final AnnouncementMapper announcementMapper;

    @Transactional
    public AnnouncementResponse create(AnnouncementCreateRequest request, AuthenticationToken token) {
        Announcement announcement = announcementRepository.save(
                announcementMapper.toEntity(request, token.getGuid())
        );

        return announcementMapper.toResponse(announcement);
    }

    @Transactional(readOnly = true)
    public AnnouncementResponseList getAnnouncements(Pageable pageable, AuthenticationToken authenticationToken) {
        return buildAnnouncementResponseList(authenticationToken.getGuid(), pageable);
    }

    @Transactional
    public AnnouncementResponseList markAsSeen(Pageable pageable, AuthenticationToken authenticationToken) {
        announcementRepository.findMaxId()
                .ifPresent(maxId -> {
                    UserAnnouncementState state = userAnnouncementStateRepository.findById(authenticationToken.getGuid())
                            .orElse(
                                    UserAnnouncementState.builder()
                                            .userGuid(authenticationToken.getGuid())
                                            .build()
                            );

                    state.setLastSeenAnnouncementId(maxId);

                    userAnnouncementStateRepository.save(state);
                });

        return buildAnnouncementResponseList(authenticationToken.getGuid(), pageable);
    }

    private AnnouncementResponseList buildAnnouncementResponseList(UUID currentUserGuid, Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findAll(pageable);

        long cursor = userAnnouncementStateRepository.findById(currentUserGuid)
                .map(UserAnnouncementState::getLastSeenAnnouncementId)
                .orElse(ZERO_CURSOR);

        long unread = announcementRepository.countByIdGreaterThan(cursor);

        return AnnouncementResponseList.builder()
                .announcements(announcementMapper.toPagedModel(announcements, announcementMapper::toResponse))
                .unread(unread)
                .build();
    }
}
