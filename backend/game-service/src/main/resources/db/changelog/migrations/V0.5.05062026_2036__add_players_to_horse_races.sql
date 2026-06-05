--liquibase formatted sql

--changeset Pavel:V0.5.05062026_2036__add_players_to_horse_races

ALTER TABLE game_horse_races
    ADD COLUMN players JSONB NOT NULL DEFAULT '[]';

CREATE INDEX idx_horse_races_players ON game_horse_races USING GIN (players);