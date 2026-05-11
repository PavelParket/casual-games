--liquibase formatted sql

--changeset Timofei:V0.2.21022026_2116__update_index_transactions_table

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS idx_transactions_room_user_unique;

DROP INDEX IF EXISTS idx_transactions_room_user_unique;
CREATE UNIQUE INDEX idx_transactions_room_user_unique ON transactions (room_id, user_guid)
    WHERE status IN ('PENDING', 'SUCCESS') AND room_id IS NOT NULL AND room_type = 'TIC_TAC_TOE';