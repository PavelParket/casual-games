--liquibase formatted sql

--changeset Pavel:V0.5.25052026_1200__add_profile_picture_columns

ALTER TABLE users
    ADD COLUMN link_profile_picture      VARCHAR(512),
    ADD COLUMN link_profile_picture_mini VARCHAR(512);