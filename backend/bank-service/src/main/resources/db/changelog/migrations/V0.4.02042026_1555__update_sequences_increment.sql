--liquibase formatted sql

--changeset Pavel:V0.4.02042026_1555__update_sequences_increment

ALTER SEQUENCE transactions_id_seq INCREMENT BY 1;

ALTER SEQUENCE transaction_summaries_id_seq INCREMENT BY 1;

ALTER SEQUENCE processed_rooms_id_seq INCREMENT BY 1;