--liquibase formatted sql

--changeset Timofei:V0.1.08012026_1543__create_transaction_summaries_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transaction_summaries'

CREATE TABLE transaction_summaries (
    id BIGSERIAL PRIMARY KEY,
    user_guid UUID NOT NULL,
    balance_before DECIMAL(19, 2) NOT NULL CHECK (balance_before >= 0),
    balance_after DECIMAL(19, 2) NOT NULL CHECK (balance_after >= 0),
    total_won DECIMAL(19, 2) NOT NULL CHECK (total_won >= 0),
    total_lost DECIMAL(19, 2) NOT NULL CHECK (total_lost >= 0),
    net_profit DECIMAL(19, 2) NOT NULL,
    summary_month DATE NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER SEQUENCE IF EXISTS transaction_summaries_id_seq START WITH 1 INCREMENT BY 10;

CREATE INDEX idx_transaction_summaries_user_guid ON transaction_summaries(user_guid);
CREATE INDEX idx_transaction_summaries_created_at ON transaction_summaries(created_at);
CREATE INDEX idx_transaction_summaries_user_month ON transaction_summaries (user_guid, summary_month);