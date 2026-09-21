package com.casualgames.notifications.service;

import com.casualgames.commonutils.exception.BadRequestException;
import com.casualgames.commonutils.exception.ForbiddenException;
import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.kafkastarter.dto.event.NotificationEvent;
import com.casualgames.notifications.domain.dto.NotificationResponse;
import com.casualgames.notifications.domain.dto.NotificationResponseList;
import com.casualgames.notifications.domain.entity.Notification;
import com.casualgames.notifications.domain.entity.NotificationTemplate;
import com.casualgames.notifications.mapper.NotificationMapper;
import com.casualgames.notifications.repository.NotificationRepository;
import com.casualgames.notifications.repository.NotificationTemplateRepository;
import com.casualgames.notifications.service.helper.PermissionHelper;
import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.enums.Operation;
import com.casualgames.securitystarter.enums.Permissions;
import com.casualgames.securitystarter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.casualgames.notifications.config.ResourceMessageConstants.BAD_REQUEST_ALREADY_MARKED_AS_READ;
import static com.casualgames.notifications.config.ResourceMessageConstants.FORBIDDEN_READ_NOTIFICATIONS;
import static com.casualgames.notifications.config.ResourceMessageConstants.FORBIDDEN_UPDATE_NOTIFICATION;
import static com.casualgames.notifications.config.ResourceMessageConstants.NOTIFICATION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String PARAM_TEMPLATE = "{%s}";

    @Value("${notification-cleanup.retention-days}")
    private int notificationCleanupRetentionDays;

    private final NotificationRepository notificationRepository;

    private final NotificationTemplateRepository notificationTemplateRepository;

    private final NotificationMapper notificationMapper;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(NotificationEvent event) {
        if (notificationRepository.existsByEventId(event.getEventId())) {
            log.info("Notification event already processed: eventId={}", event.getEventId());
            return;
        }

        NotificationTemplate template = notificationTemplateRepository.findByType(event.getType().name())
                .filter(NotificationTemplate::isEnabled)
                .orElse(null);

        if (template == null) {
            log.warn("No enabled template for type={}, eventId={} — skipping", event.getType(), event.getEventId());
            return;
        }

        Map<String, String> params = Optional.ofNullable(event.getParams())
                .orElse(Map.of());

        try {
            notificationRepository.save(
                    notificationMapper.toEntity(
                            event,
                            renderParams(template.getTitleTemplate(), params),
                            renderParams(template.getBodyTemplate(), params),
                            renderParams(template.getLinkTemplate(), params)
                    )
            );
        } catch (DataIntegrityViolationException e) {
            log.info("Notification event was processed by another request: eventId={}", event.getEventId());
        }
    }

    private String renderParams(String template, Map<String, String> params) {
        if (template == null) {
            return null;
        }

        String text = template;

        for (Map.Entry<String, String> param : params.entrySet()) {
            text = text.replace(
                    String.format(PARAM_TEMPLATE, param.getKey()),
                    param.getValue()
            );
        }

        return text;
    }

    @Transactional(readOnly = true)
    public NotificationResponseList getNotifications(Pageable pageable, AuthenticationToken authenticationToken) {
        if (!permissionValidator.hasAccess(Permissions.NOTIFICATION, Operation.READ,
                permissionHelper.getContext(authenticationToken.getGuid()), authenticationToken)) {
            throw new ForbiddenException(FORBIDDEN_READ_NOTIFICATIONS);
        }

        return buildNotificationList(authenticationToken.getGuid(), pageable);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, AuthenticationToken authenticationToken) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        if (!permissionValidator.hasAccess(Permissions.NOTIFICATION, Operation.UPDATE,
                permissionHelper.getContext(notification.getRecipientGuid()), authenticationToken)) {
            throw new ForbiddenException(FORBIDDEN_UPDATE_NOTIFICATION);
        }

        if (notification.getReadAt() != null) {
            throw new BadRequestException(BAD_REQUEST_ALREADY_MARKED_AS_READ);
        }

        notification.setReadAt(Instant.now());

        return notificationMapper.toResponse(
                notificationRepository.save(notification)
        );
    }

    @Transactional
    public NotificationResponseList markAllAsRead(Pageable pageable, AuthenticationToken authenticationToken) {
        if (!permissionValidator.hasAccess(Permissions.NOTIFICATION, Operation.UPDATE,
                permissionHelper.getContext(authenticationToken.getGuid()), authenticationToken)) {
            throw new ForbiddenException(FORBIDDEN_UPDATE_NOTIFICATION);
        }

        notificationRepository.markAllAsRead(authenticationToken.getGuid(), Instant.now());

        return buildNotificationList(authenticationToken.getGuid(), pageable);
    }

    private NotificationResponseList buildNotificationList(UUID recipientGuid, Pageable pageable) {
        Instant now = Instant.now();
        Page<Notification> notifications = notificationRepository.findByRecipientGuid(recipientGuid, now, pageable);
        long unread = notificationRepository.countByRecipientGuidAndReadAtIsNull(recipientGuid, now);

        return NotificationResponseList.builder()
                .notifications(notificationMapper.toPagedModel(notifications, notificationMapper::toResponse))
                .unread(unread)
                .build();
    }

    @Transactional
    public void deleteReadNotifications(Instant date) {
        date = date.minus(notificationCleanupRetentionDays, ChronoUnit.DAYS);
        notificationRepository.deleteRead(date);
    }
}
