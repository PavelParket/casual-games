--liquibase formatted sql

--changeset Timofei:V0.1.10012026_1657__create_auths_table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'auths'

CREATE TABLE auths (
	id BIGSERIAL PRIMARY KEY,
	guid UUID NOT NULL UNIQUE,
	username VARCHAR(50) NOT NULL,
	email VARCHAR(200) NOT NULL UNIQUE,
	password VARCHAR(60) NOT NULL,
	role VARCHAR(255) NOT NULL,
	created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX ux_auths_guid ON auths(guid);
CREATE UNIQUE INDEX ux_auths_email ON auths(email);