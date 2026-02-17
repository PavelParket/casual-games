--liquibase formatted sql

--changeset Pavel:V0.2.16022026_1800__init_horse_races_table

CREATE TABLE game_horse_races (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL,
    server_seed VARCHAR(255) NOT NULL,
    seed_hash VARCHAR(255) NOT NULL,
    horse_count SMALLINT NOT NULL,
    winner_horse_index SMALLINT NOT NULL,
    segments_count SMALLINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'RUNNING',
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_horse_races_room_id ON game_horse_races (room_id);
CREATE INDEX idx_horse_races_status ON game_horse_races (status);