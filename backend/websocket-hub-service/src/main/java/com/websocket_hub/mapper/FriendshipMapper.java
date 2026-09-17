package com.websocket_hub.mapper;

import com.kafka_starter.dto.event.sync.SynchronizedFriendship;
import com.websocket_hub.domain.entity.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface FriendshipMapper {

    void updateEntity(@MappingTarget Friendship friendship, SynchronizedFriendship synchronizedFriendship);

    default void updateEntities(List<Friendship> friendships, Map<Long, SynchronizedFriendship> synchronizedFriendshipMap) {
        friendships.forEach(friendship ->
                updateEntity(friendship, synchronizedFriendshipMap.get(friendship.getId()))
        );
    }
}
