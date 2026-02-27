--liquibase formatted sql

--changeset Timofei:V0.1.21022026_2116__update_index_transactions_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transactions'

ALTER TABLE transactions DROP CONSTRAINT IF EXISTS idx_transactions_room_user_unique;
DROP INDEX IF EXISTS idx_transactions_room_user_unique;
CREATE UNIQUE INDEX idx_transactions_room_user_unique
    ON transactions(room_id, user_guid)
    WHERE status IN ('PENDING', 'SUCCESS') AND room_id IS NOT NULL AND room_type = 'TIC_TAC_TOE';