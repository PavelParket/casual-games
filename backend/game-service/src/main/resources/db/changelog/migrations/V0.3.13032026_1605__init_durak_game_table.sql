--liquibase formatted sql

--changeset Pavel:V0.3.13032026_1605__init_durak_game_table

CREATE TABLE game_durak (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    winner_id UUID,
    players JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

--rollback DROP TABLE game_durak;

CREATE INDEX idx_game_durak_status ON game_durak (status);
CREATE INDEX idx_game_durak_winner_id ON game_durak (winner_id);
CREATE INDEX idx_game_durak_players ON game_durak USING GIN (players);

--rollback DROP INDEX idx_game_durak_players;
--rollback DROP INDEX idx_game_durak_winner_id;
--rollback DROP INDEX idx_game_durak_status;