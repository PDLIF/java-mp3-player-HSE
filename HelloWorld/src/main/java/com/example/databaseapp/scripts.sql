-- Создание базы данных
CREATE DATABASE smart_home_db;

-- Используем базу данных
\c smart_home_db;

-- Создание таблицы устройств
CREATE TABLE IF NOT EXISTS devices (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(100) NOT NULL,
    status BOOLEAN NOT NULL
);

-- Хранимая процедура для очистки таблицы устройств
CREATE OR REPLACE PROCEDURE clear_devices_table()
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM devices;
END;
$$;

-- Хранимая процедура для добавления нового устройства
CREATE OR REPLACE PROCEDURE insert_device(
    p_name VARCHAR(100),
    p_type VARCHAR(100),
    p_status BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO devices (name, type, status)
    VALUES (p_name, p_type, p_status);
END;
$$;

-- Хранимая функция для поиска устройства по названию
CREATE OR REPLACE FUNCTION search_device(p_name VARCHAR(100))
RETURNS TABLE (id INT, name VARCHAR, type VARCHAR, status BOOLEAN)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY SELECT * FROM devices WHERE name = p_name;
END;
$$;

-- Хранимая процедура для обновления статуса устройства
CREATE OR REPLACE PROCEDURE update_device_status(
    p_id INT,
    p_status BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE devices SET status = p_status WHERE id = p_id;
END;
$$;

-- Хранимая процедура для удаления устройства по названию
CREATE OR REPLACE PROCEDURE delete_device_by_name(p_name VARCHAR(100))
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM devices WHERE name = p_name;
END;
$$;

-- Хранимая процедура для создания пользователя с ролью
CREATE OR REPLACE PROCEDURE create_user(
    p_username VARCHAR(50),
    p_password VARCHAR(50),
    p_role VARCHAR(20)
)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', p_username, p_password);
    IF p_role = 'admin' THEN
        EXECUTE format('GRANT ALL PRIVILEGES ON DATABASE smart_home_db TO %I', p_username);
    ELSE
        EXECUTE format('GRANT CONNECT ON DATABASE smart_home_db TO %I', p_username);
        EXECUTE format('GRANT SELECT ON ALL TABLES IN SCHEMA public TO %I', p_username);
    END IF;
END;
$$;
