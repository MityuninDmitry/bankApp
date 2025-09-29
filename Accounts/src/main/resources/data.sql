-- Очистка данных перед вставкой
DELETE FROM accounts.payment_accounts;
DELETE FROM accounts.users;

-- Вставка пользователей и получение их ID
INSERT INTO accounts.users (login, password, first_name, last_name, email, birth_date) VALUES
('testUser', 'encrypted_pass_1', 'Иван', 'Иванов', 'ivan@mail.com', '1990-01-15');

INSERT INTO accounts.users (login, password, first_name, last_name, email, birth_date) VALUES
('john_doe', 'encrypted_pass_2', 'John', 'Doe', 'john@mail.com', '1985-05-20');

-- Вставка счетов с динамическим получением user_id
INSERT INTO accounts.payment_accounts (account_number, currency, balance, is_deleted, user_id) 
SELECT 'ACC123456', 0, 10000.00, false, id
FROM accounts.users WHERE login = 'testUser';

INSERT INTO accounts.payment_accounts (account_number, currency, balance, is_deleted, user_id) 
SELECT 'ACC789012',1, 500.00, false, id
FROM accounts.users WHERE login = 'testUser';

INSERT INTO accounts.payment_accounts (account_number, currency, balance, is_deleted, user_id) 
SELECT 'ACC_SUCCESS', 0, 15000.00, false, id
FROM accounts.users WHERE login = 'testUser';

INSERT INTO accounts.payment_accounts (account_number, currency, balance, is_deleted, user_id) 
SELECT 'ACC_INSUFFICIENT', 0, 1000.00, false, id
FROM accounts.users WHERE login = 'testUser';

INSERT INTO accounts.payment_accounts (account_number, currency, balance, is_deleted, user_id) 
SELECT 'ACC_DELETED', 1, 500.00, true, id
FROM accounts.users WHERE login = 'testUser';