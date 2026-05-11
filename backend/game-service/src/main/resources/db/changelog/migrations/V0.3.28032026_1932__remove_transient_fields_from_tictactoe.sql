--liquibase formatted sql

--changeset Timofei:V0.3.28032026_1932__remove_transient_fields_from_tictactoe

DROP INDEX IF EXISTS idx_tic_tac_toe_player_x;
DROP INDEX IF EXISTS idx_tic_tac_toe_player_o;

ALTER TABLE game_tic_tac_toe
    DROP COLUMN IF EXISTS board,
    DROP COLUMN IF EXISTS player_x_id,
    DROP COLUMN IF EXISTS player_o_id;