package casualgames.userservice.service;

import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final int INITIAL_PAGE = 0;
    private final int BATCH_SIZE = 100;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final KafkaMessageHelper kafkaMessageHelper;

    @Async
    @Transactional
    public void processFullSyncUsersAndFriendships() {
        log.info("Starting full sync users and friendships at {}", Instant.now());

        int page = INITIAL_PAGE;
        int totalPages = userRepository.findAll(PageRequest.of(page, BATCH_SIZE)).getTotalPages();

        do {
            userRepository.findAll(PageRequest.of(page, BATCH_SIZE))
                    .forEach(user -> {
                        kafkaMessageHelper.save(
                                kafkaMessageHelper.getTopics().getUser(),
                                user.getGuid().toString(),
                                kafkaMessageHelper.buildSynchronizedUserMessage(user)
                        );

                        processFriendsSync(user);
                    });

            page++;
        } while (page < totalPages);
    }

    private void processFriendsSync(User user) {
        int page = INITIAL_PAGE;
        int totalPages = friendshipRepository.findByUserGuid(
                        user.getGuid(),
                        PageRequest.of(page, BATCH_SIZE)
                )
                .getTotalPages();

        do {
            List<Friendship> friendships = friendshipRepository.findByUserGuid(
                            user.getGuid(),
                            PageRequest.of(page, BATCH_SIZE)
                    )
                    .getContent();

            kafkaMessageHelper.save(
                    kafkaMessageHelper.getTopics().getFriendship(),
                    user.getGuid().toString(),
                    kafkaMessageHelper.buildSynchronizedFriendshipFullSyncEvent(user.getGuid(), friendships)
            );

            page++;
        } while (page < totalPages);
    }
}
