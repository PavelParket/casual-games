--liquibase formatted sql

--changeset Pavel:V0.4.17042026_1116__create_transactional_outbox_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transactional_outbox_kafka_events'
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM pg_indexes WHERE table_name = 'transactional_outbox_kafka_events' AND index_name = 'idx_outbox_unsent'

CREATE TABLE transactional_outbox_kafka_events
(
    id              UUID         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    topic           VARCHAR(255) NOT NULL,
    message_id      UUID         NOT NULL             DEFAULT gen_random_uuid(),
    message_payload JSONB        NOT NULL,
    created_date    TIMESTAMP    NOT NULL             DEFAULT now(),
    sent            BOOLEAN      NOT NULL             DEFAULT FALSE
);

CREATE INDEX idx_outbox_unsent ON transactional_outbox_kafka_events (sent, created_date) WHERE sent = FALSE;