package casualgames.userservice.service;

import casualgames.userservice.domain.entity.Friendship;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.repository.FriendshipRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
        Page<User> userPage;

        do {
            userPage = userRepository.findAll(PageRequest.of(page, BATCH_SIZE));

            userPage.getContent().forEach(user -> {
                kafkaMessageHelper.save(
                        kafkaMessageHelper.getTopics().getUser(),
                        user.getGuid().toString(),
                        kafkaMessageHelper.buildSynchronizedUserMessage(user)
                );

                processFriendsSync(user);
            });

            page++;
        } while (userPage.hasNext());
    }

    private void processFriendsSync(User user) {
        int page = INITIAL_PAGE;
        Page<Friendship> friendshipPage;

        do {
            friendshipPage = friendshipRepository.findByUserGuid(user.getGuid(), PageRequest.of(page, BATCH_SIZE));

            kafkaMessageHelper.save(
                    kafkaMessageHelper.getTopics().getFriendship(),
                    user.getGuid().toString(),
                    kafkaMessageHelper.buildSynchronizedFriendshipFullSyncEvent(user.getGuid(), friendshipPage.getContent())
            );

            page++;
        } while (friendshipPage.hasNext());
    }
}
