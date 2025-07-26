-- Создаем обе схемы
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS exchange_generator;

-- Даем права пользователю на обе схемы
GRANT ALL PRIVILEGES ON SCHEMA accounts TO accounts_user;
GRANT ALL PRIVILEGES ON SCHEMA exchange_generator TO accounts_user;