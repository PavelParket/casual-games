package com.websocket_hub.validator;

import com.websocket_hub.domain.dto.request.RoomRequest;
import com.websocket_hub.domain.entity.RoomMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Component
@Slf4j
public class RoomValidator {

    public boolean isRoomNameExists(RoomRequest roomRequest, Set<RoomMetadata> metadata) {
        if (CollectionUtils.isEmpty(metadata)) {
            return false;
        }

        return metadata.stream()
                .anyMatch(roomMetadata ->
                        roomMetadata.getName().equals(roomRequest.roomName())
                );
    }
}
