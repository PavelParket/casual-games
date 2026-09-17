--liquibase formatted sql

--changeset Pavel:V0.8.03082026_1746__add_friend_removed_notification_template

INSERT INTO notification_templates (type, title_template, body_template)
VALUES ('FRIEND_REMOVED', 'Friend removed', '{username} removed you from friends list.');

UPDATE notification_templates
SET link_template = '/room/{roomHandler}/{roomName}/{roomId}'
WHERE type = 'ROOM_INVITE';
