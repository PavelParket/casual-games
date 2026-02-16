--liquibase formatted sql

--changeset Pavel:V0.2.16022026_1800__init_horse_races_table

CREATE TABLE horse_races (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL,
    server_seed VARCHAR(255) NOT NULL,
    seed_hash VARCHAR(255) NOT NULL,
    winner_horse_index BIGINT NOT NULL,
    segments_count BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SIMULATED',
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_horse_races_room_id ON horse_races (room_id);
CREATE INDEX idx_horse_races_status ON horse_races (status);