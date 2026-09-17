package com.kafka_starter.dto.event.sync;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronizedUser {

    private Long id;

    private UUID guid;

    private String username;

    private String email;

    private String role;

    private String status;

    private String linkProfilePicture;

    private String linkProfilePictureMini;

    private Instant createdAt;
}
