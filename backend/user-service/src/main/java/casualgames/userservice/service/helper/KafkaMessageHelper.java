package casualgames.userservice.service.helper;

import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import com.common_utils.enums.NotificationType;
import com.kafka_starter.config.KafkaTopics;
import com.kafka_starter.dto.event.NotificationEvent;
import com.kafka_starter.dto.event.UpdateSubscriptionEvent;
import com.kafka_starter.dto.event.sync.SynchronizedFriendship;
import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.kafka_starter.service.KafkaTransactionalOutboxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageHelper {

    private static final String COLON_DELIMITER = ":";

    private final KafkaTopics kafkaTopics;

    private final KafkaTransactionalOutboxMessageService kafkaTransactionalOutboxMessageService;

    public KafkaTopics getTopics() {
        return this.kafkaTopics;
    }

    public void save(String topic, Object payload) {
        kafkaTransactionalOutboxMessageService.save(topic, payload);
    }

    public void save(String topic, String partitionKey, Object payload) {
        kafkaTransactionalOutboxMessageService.save(topic, partitionKey, payload);
    }

    public SynchronizedUser buildSynchronizedUserMessage(User user) {
        return SynchronizedUser.builder()
                .id(user.getId())
                .guid(user.getGuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .linkProfilePicture(user.getLinkProfilePicture())
                .linkProfilePictureMini(user.getLinkProfilePictureMini())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UpdateSubscriptionEvent buildUpdateSubscriptionEvent(UUID userGuid,
                                                                String type,
                                                                BigDecimal amount,
                                                                BigDecimal balanceBefore,
                                                                BigDecimal balanceAfter) {
        return UpdateSubscriptionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userGuid(userGuid)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .timestamp(Instant.now())
                .build();
    }

    public NotificationEvent buildSubscriptionExpiringInDaysEvent(UUID userGuid,
                                                                  Instant expiresAt,
                                                                  Map<String, String> params) {
        String seed = String.join(COLON_DELIMITER, userGuid.toString(), NotificationType.SUBSCRIPTION_EXPIRING_SOON.name(), expiresAt.toString());
        UUID eventId = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));

        return NotificationEvent.builder()
                .eventId(eventId)
                .recipientGuid(userGuid)
                .type(NotificationType.SUBSCRIPTION_EXPIRING_SOON)
                .params(params)
                .timestamp(Instant.now())
                .build();
    }

    public NotificationEvent buildFriendRequestUpdatedEvent(UUID userGuid,
                                                            Long requestId,
                                                            NotificationType notificationType,
                                                            Map<String, String> params) {
        String seed = String.join(COLON_DELIMITER, userGuid.toString(), notificationType.name(), requestId.toString());
        UUID eventId = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));

        return NotificationEvent.builder()
                .eventId(eventId)
                .recipientGuid(userGuid)
                .type(notificationType)
                .params(params)
                .timestamp(Instant.now())
                .build();
    }

    public NotificationEvent buildFriendRemovedEvent(UUID userGuid, Long friendshipId, Map<String, String> params) {
        String seed = String.join(COLON_DELIMITER, userGuid.toString(), NotificationType.FRIEND_REMOVED.name(), friendshipId.toString());
        UUID eventId = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));

        return NotificationEvent.builder()
                .eventId(eventId)
                .recipientGuid(userGuid)
                .type(NotificationType.FRIEND_REMOVED)
                .params(params)
                .timestamp(Instant.now())
                .build();
    }

    public SynchronizedFriendship buildSynchronizedFriendshipEvent(String type, Friendship friendship) {
        return SynchronizedFriendship.builder()
                .type(type)
                .id(friendship.getId())
                .userGuid(friendship.getUserGuid())
                .friendGuid(friendship.getFriendGuid())
                .createdAt(friendship.getCreatedAt())
                .build();
    }

    // todo: вынести в абстракцию с дженериками
    public String friendshipPartitionKey(UUID userGuid, UUID friendGuid) {
        boolean first = userGuid.compareTo(friendGuid) <= 0;
        UUID user = first ? userGuid : friendGuid;
        UUID friend = first ? friendGuid : userGuid;
        return user + COLON_DELIMITER + friend;
    }
}
