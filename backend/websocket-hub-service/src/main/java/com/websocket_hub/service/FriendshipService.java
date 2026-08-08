package com.websocket_hub.service;

import com.kafka_starter.dto.event.sync.SynchronizedFriendship;
import com.websocket_hub.domain.entity.Friendship;
import com.websocket_hub.domain.repository.FriendshipRepository;
import com.websocket_hub.mapper.FriendshipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

    private static final String CREATED = "CREATED";
    private static final String REMOVED = "REMOVED";
    private static final String FULL_SYNC = "FULL_SYNC";

    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapper friendshipMapper;

    @Transactional
    public void synchronizeUpdatedFriendship(SynchronizedFriendship synchronizedFriendship) {
        switch (synchronizedFriendship.getType()) {
            case CREATED -> updateFriendship(synchronizedFriendship);
            case REMOVED -> deleteFriendship(synchronizedFriendship);
            case FULL_SYNC -> fullSync(synchronizedFriendship);
            default -> log.warn("Unknown friendship sync event type: {}", synchronizedFriendship.getType());
        }
    }

    private void updateFriendship(SynchronizedFriendship synchronizedFriendship) {
        Friendship friendship = friendshipRepository.findById(synchronizedFriendship.getId())
                .orElseGet(() ->
                        Friendship.builder()
                                .id(synchronizedFriendship.getId())
                                .build()
                );

        friendshipMapper.updateEntity(friendship, synchronizedFriendship);
        friendshipRepository.save(friendship);
    }

    private void deleteFriendship(SynchronizedFriendship synchronizedFriendship) {
        if (!friendshipRepository.existsById(synchronizedFriendship.getId())) {
            return;
        }

        friendshipRepository.deleteById(synchronizedFriendship.getId());
    }

    private void fullSync(SynchronizedFriendship synchronizedFriendship) {
        List<Long> friendshipIds = synchronizedFriendship.getSynchronizedFriendshipList()
                .stream()
                .map(SynchronizedFriendship::getId)
                .toList();

        friendshipRepository.deleteAllById(friendshipIds);

        List<Friendship> updatedFriendships = synchronizedFriendship.getSynchronizedFriendshipList()
                .stream()
                .map(syncFriendship ->
                        Friendship.builder()
                                .id(syncFriendship.getId())
                                .build()
                )
                .collect(Collectors.toList());

        Map<Long, SynchronizedFriendship> synchronizedFriendshipMap = synchronizedFriendship.getSynchronizedFriendshipList()
                .stream()
                .collect(Collectors.toMap(
                        SynchronizedFriendship::getId,
                        Function.identity()
                ));

        friendshipMapper.updateEntities(updatedFriendships, synchronizedFriendshipMap);
        friendshipRepository.saveAll(updatedFriendships);
    }
}
