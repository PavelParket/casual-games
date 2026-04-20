--liquibase formatted sql

--changeset Pavel:V0.4.02042026_1555__update_sequences_increment
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transactions'
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transaction_summaries'
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'processed_rooms'

ALTER SEQUENCE transactions_id_seq INCREMENT BY 1;

ALTER SEQUENCE transaction_summaries_id_seq INCREMENT BY 1;

ALTER SEQUENCE processed_rooms_id_seq INCREMENT BY 1;