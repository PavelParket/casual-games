--liquibase formatted sql

--changeset Timofei:V0.2.17022026_1218__init_tictactoe_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'game_tictactoe'

CREATE TABLE game_tic_tac_toe (
                                   id BIGSERIAL PRIMARY KEY,
                                   room_id UUID NOT NULL UNIQUE,
                                   player_x_id UUID NOT NULL,
                                   player_o_id UUID NOT NULL,
                                   winner_id UUID,
                                   event VARCHAR(255) NOT NULL,
                                   board JSONB,
                                   players JSONB,
                                   version BIGINT NOT NULL DEFAULT 0,
                                   created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_tictactoe_player_x ON tictactoe_games(player_x_id);
CREATE INDEX idx_tictactoe_player_o ON tictactoe_games(player_o_id);