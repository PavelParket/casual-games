package com.game_service.horse_race.domain.entity;

import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "game_horse_races")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HorseRace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private String serverSeed;

    @Column(nullable = false)
    private String seedHash;

    @Column(nullable = false)
    private Integer horseCount;

    @Column(nullable = false)
    private Integer winnerHorseIndex;

    @Column(nullable = false)
    private Integer segmentsCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HorseRaceStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
