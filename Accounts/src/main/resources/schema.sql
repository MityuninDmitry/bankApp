CREATE SCHEMA IF NOT EXISTS accounts;

CREATE TABLE accounts.users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    birth_date DATE
);

CREATE TABLE accounts.payment_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_number VARCHAR(255) UNIQUE NOT NULL,
    currency VARCHAR(50) NOT NULL,
    balance DECIMAL(19,2) NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES accounts.users(id)
);

-- Тестовые данные (теперь нужно указать колонки явно, кроме id)
INSERT INTO accounts.users (login, password, first_name, last_name, email, birth_date) VALUES
('testUser', 'encrypted_pass_1', 'Иван', 'Иванов', 'ivan@mail.com', '1990-01-15'),
('john_doe', 'encrypted_pass_2', 'John', 'Doe', 'john@mail.com', '1985-05-20');

-- Вставка тестовых счетов
 INSERT INTO accounts.payment_accounts (account_number, currency, balance, is_deleted, user_id) VALUES
 ('ACC123456', 0, 10000.00, false, 1),  -- 0 = RUB
 ('ACC789012', 1, 500.00, false, 1),    -- 1 = USD
 ('ACC_SUCCESS', 0, 15000.00, false, 1), -- 0 = RUB
 ('ACC_INSUFFICIENT', 0, 1000.00, false, 1), -- 0 = RUB
 ('ACC_DELETED', 1, 500.00, true, 1);   -- 1 = USD