package casualgames.userservice.config.initializer;

import casualgames.userservice.repository.FriendshipRepository;
import com.redis_starter.repository.RedisSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static casualgames.userservice.service.FriendshipService.friendsKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendCacheInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final FriendshipRepository friendshipRepository;

    private final RedisSetRepository redisSetRepository;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent applicationReadyEvent) {
        Map<String, List<String>> friendsByKey = new HashMap<>();

        friendshipRepository.findAll().forEach(friendship -> {
            friendsByKey.computeIfAbsent(
                            friendsKey(friendship.getUserGuid()),
                            k -> new ArrayList<>()
                    )
                    .add(friendship.getFriendGuid().toString());
            friendsByKey.computeIfAbsent(
                            friendsKey(friendship.getFriendGuid()),
                            k -> new ArrayList<>()
                    )
                    .add(friendship.getUserGuid().toString());
        });

        friendsByKey.forEach(redisSetRepository::addAll);

        log.info("Friend cache backfill complete: {} keys", friendsByKey.size());
    }
}
