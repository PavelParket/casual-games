--liquibase formatted sql

--changeset Timofei:V0.1.07012026_1103__create_transactions_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transactions'

CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              room_id UUID NOT NULL,
                              room_type VARCHAR(255) NOT NULL,
                              user_guid UUID NOT NULL,
                              type VARCHAR(255) NOT NULL,
                              status VARCHAR(255) NOT NULL,
                              amount DECIMAL(19, 2) NOT NULL CHECK (amount >= 0),
                              balance_before DECIMAL(19, 2) NOT NULL CHECK (balance_before >= 0),
                              balance_after DECIMAL(19, 2) NOT NULL CHECK (balance_after >= 0),
                              created_at TIMESTAMP NOT NULL,
                              version BIGINT NOT NULL DEFAULT 0
);

ALTER SEQUENCE IF EXISTS transactions_id_seq START WITH 1 INCREMENT BY 15;

CREATE INDEX idx_transactions_user_guid ON transactions(user_guid);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_room_id ON transactions(room_id);

CREATE UNIQUE INDEX idx_transactions_room_user_unique
    ON transactions(room_id, user_guid)
    WHERE status IN ('PENDING', 'SUCCESS');