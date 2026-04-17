--liquibase formatted sql

--changeset Pavel:V0.4.17042026_1116__create_transactional_outbox_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transactional_outbox_kafka_messages'
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'transactional_outbox_kafka_messages' AND indexname = 'idx_outbox_unsent'

CREATE TABLE transactional_outbox_kafka_messages
(
    id              UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    topic           VARCHAR(255) NOT NULL,
    message_id      UUID         NOT NULL             DEFAULT gen_random_uuid(),
    message_payload JSONB        NOT NULL,
    sent            BOOLEAN      NOT NULL             DEFAULT FALSE,
    created_date    TIMESTAMP    NOT NULL             DEFAULT now()
);

CREATE INDEX idx_outbox_unsent ON transactional_outbox_kafka_messages (sent, created_date) WHERE sent = FALSE;