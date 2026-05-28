package casualgames.userservice.service.helper;

import casualgames.userservice.domain.entity.User;
import com.kafka_starter.config.KafkaTopics;
import com.kafka_starter.dto.event.UpdateSubscriptionEvent;
import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.kafka_starter.service.KafkaTransactionalOutboxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageHelper {

    private final KafkaTransactionalOutboxMessageService kafkaTransactionalOutboxMessageService;

    private final KafkaTopics kafkaTopics;

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
}
