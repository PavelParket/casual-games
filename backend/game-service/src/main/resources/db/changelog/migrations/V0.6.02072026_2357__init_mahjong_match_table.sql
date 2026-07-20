--liquibase formatted sql

--changeset Pavel:V0.6.02072026_2357__init_mahjong_match_table

CREATE TABLE game_mahjong
(
    id            BIGSERIAL PRIMARY KEY,
    room_id       UUID        NOT NULL,
    status        VARCHAR(50) NOT NULL,
    winner_id     UUID,
    players       JSONB       NOT NULL,
    seed          BIGINT      NOT NULL,
    tiles_cleared JSONB,
    created_at    TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_game_mahjong_status ON game_mahjong (status);
CREATE INDEX idx_game_mahjong_winner_id ON game_mahjong (winner_id);
CREATE INDEX idx_game_mahjong_players ON game_mahjong USING GIN (players);
