--liquibase formatted sql

--changeset Timofei:V0.1.29012026_2034__update_transactions_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transactions'

ALTER TABLE transactions ALTER COLUMN room_id DROP NOT NULL;
ALTER TABLE transactions ALTER COLUMN room_type DROP NOT NULL;

DROP INDEX IF EXISTS idx_transactions_room_id;
CREATE INDEX idx_transactions_room_id ON transactions(room_id) WHERE room_id IS NOT NULL;

DROP INDEX IF EXISTS idx_transactions_room_user_unique;
CREATE UNIQUE INDEX idx_transactions_room_user_unique
    ON transactions(room_id, user_guid)
    WHERE status IN ('PENDING', 'SUCCESS') AND room_id IS NOT NULL;