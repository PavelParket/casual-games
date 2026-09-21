package com.casualgames.websockethub.mapper;

import com.casualgames.websockethub.domain.dto.client.DeCoderGameInternalRequest;
import com.casualgames.websockethub.domain.dto.client.DeCoderGameInternalResponse;
import com.casualgames.websockethub.domain.dto.message.DeCoderGameMessage;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.DeCoderGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameMessageMapper extends MessageMapper {

    @Mapping(target = "player", ignore = true)
    @Mapping(target = "code", ignore = true)
    DeCoderGameInternalRequest toStartRequest(DeCoderGameEvent event,
                                              UUID roomId);

    DeCoderGameInternalRequest toMoveRequest(DeCoderGameEvent event,
                                             UUID roomId,
                                             UUID player,
                                             String code);

    @Mapping(target = "code", ignore = true)
    DeCoderGameMessage toMessage(DeCoderGameInternalResponse deCoderGameInternalResponse,
                                 MessageType type,
                                 UUID fromUserId,
                                 UUID toUserId,
                                 BigDecimal balanceBefore,
                                 BigDecimal spent);
}