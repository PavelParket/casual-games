-- Создание схем для всех сервисов при первой инициализации контейнера.
-- Liquibase создаёт таблицы ВНУТРИ схем, но сами схемы должны существовать заранее.
-- Выполняется автоматически при первом старте postgres (пустой volume).

CREATE SCHEMA IF NOT EXISTS security_service;
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS bank_service;
CREATE SCHEMA IF NOT EXISTS game_service;
CREATE SCHEMA IF NOT EXISTS websocket_hub;
CREATE SCHEMA IF NOT EXISTS notifications;
