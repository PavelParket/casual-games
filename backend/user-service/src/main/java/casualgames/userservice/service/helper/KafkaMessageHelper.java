package casualgames.userservice.service.helper;

import casualgames.userservice.entity.User;
import com.kafka_starter.config.KafkaTopics;
import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.kafka_starter.service.KafkaTransactionalOutboxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public SynchronizedUser buildMessage(User user) {
        return SynchronizedUser.builder()
                .guid(user.getGuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
