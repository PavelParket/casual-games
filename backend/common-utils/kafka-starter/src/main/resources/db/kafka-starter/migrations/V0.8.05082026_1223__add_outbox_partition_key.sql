--liquibase formatted sql

--changeset Pavel:V0.8.05082026_1223__add_outbox_partition_key

ALTER TABLE transactional_outbox_kafka_messages
    ADD COLUMN partition_key VARCHAR(255);
