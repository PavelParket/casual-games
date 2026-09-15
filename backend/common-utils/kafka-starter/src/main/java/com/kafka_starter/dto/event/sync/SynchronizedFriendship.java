package com.kafka_starter.dto.event.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronizedFriendship {

    // todo: переделать этот костыль на нормальный абстрактный объект с поле для оператора update/delete
    private String type;

    private Long id;

    private UUID userGuid;

    private UUID friendGuid;

    private Instant createdAt;

    // todo: переделать этот костыль на нормальный синк батчами
    private List<SynchronizedFriendship> synchronizedFriendshipList;
}
