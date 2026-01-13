--liquibase formatted sql

--changeset Timofei:V0.1.09012026_2048__create_processed_rooms_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'processed_rooms'

CREATE TABLE processed_rooms (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL UNIQUE,
    room_type VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_count INT NOT NULL,

    CONSTRAINT uq_processed_rooms_room_id UNIQUE (room_id)
);

ALTER SEQUENCE IF EXISTS processed_rooms_id_seq START WITH 1 INCREMENT BY 15;

CREATE INDEX idx_processed_rooms_type ON processed_rooms (room_type);
CREATE INDEX idx_processed_rooms_processed_at ON processed_rooms (processed_at);