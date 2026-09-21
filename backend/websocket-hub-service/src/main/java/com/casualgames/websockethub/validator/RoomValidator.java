package com.casualgames.websockethub.validator;

import com.casualgames.websockethub.domain.dto.request.RoomRequest;
import com.casualgames.websockethub.domain.entity.RoomMetadata;
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
