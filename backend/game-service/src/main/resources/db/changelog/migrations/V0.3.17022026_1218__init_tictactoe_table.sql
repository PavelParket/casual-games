--liquibase formatted sql

--changeset Timofei:V0.3.17022026_1218__init_tictactoe_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'game_tic_tac_toe'

CREATE TABLE game_tic_tac_toe (
                                   id BIGSERIAL PRIMARY KEY,
                                   room_id UUID NOT NULL UNIQUE,
                                   player_x_id UUID NOT NULL,
                                   player_o_id UUID NOT NULL,
                                   winner_id UUID,
                                   status VARCHAR(50) NOT NULL,
                                   board JSONB,
                                   players JSONB,
                                   version BIGINT NOT NULL DEFAULT 0,
                                   created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_tic_tac_toe_player_x ON game_tic_tac_toe(player_x_id);
CREATE INDEX idx_tic_tac_toe_player_o ON game_tic_tac_toe(player_o_id);