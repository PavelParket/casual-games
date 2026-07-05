package casualgames.userservice.service.helper;

import casualgames.userservice.domain.entity.User;
import com.common_utils.enums.NotificationType;
import com.kafka_starter.config.KafkaTopics;
import com.kafka_starter.dto.event.NotificationEvent;
import com.kafka_starter.dto.event.UpdateSubscriptionEvent;
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

    public SynchronizedUser buildSynchronizedUserMessage(User user) {
        return SynchronizedUser.builder()
                .guid(user.getGuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
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
}
