package com.casualgames.websockethub.domain.dto.event;

import java.util.UUID;

public record CountdownExpiredEvent(

        UUID roomId

) {
}
