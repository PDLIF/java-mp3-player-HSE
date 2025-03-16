-- Создание таблицы устройств
CREATE TABLE IF NOT EXISTS devices (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    status BOOLEAN DEFAULT false
);

-- Очистка таблицы устройств
CREATE OR REPLACE PROCEDURE clear_devices_table()
LANGUAGE plpgsql AS $$
BEGIN
    TRUNCATE TABLE devices RESTART IDENTITY;
END;
$$;

-- Добавление нового устройства
CREATE OR REPLACE PROCEDURE insert_device(device_name VARCHAR, device_type VARCHAR, device_status BOOLEAN)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO devices (name, type, status) VALUES (device_name, device_type, device_status);
END;
$$;

-- Поиск устройства по названию
CREATE OR REPLACE FUNCTION search_device(device_name VARCHAR)
RETURNS TABLE(id INT, name VARCHAR, type VARCHAR, status BOOLEAN) AS $$
BEGIN
    RETURN QUERY SELECT * FROM devices WHERE name ILIKE '%' || device_name || '%';
END;
$$ LANGUAGE plpgsql;

-- Обновление статуса устройства
CREATE OR REPLACE PROCEDURE update_device_status(device_id INT, new_status BOOLEAN)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE devices SET status = new_status WHERE id = device_id;
END;
$$;

-- Удаление устройства по названию
CREATE OR REPLACE PROCEDURE delete_device_by_name(device_name VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM devices WHERE name = device_name;
END;
$$;

-- Создание пользователя с правами доступа
CREATE OR REPLACE PROCEDURE create_user(username VARCHAR, user_password TEXT, role VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', username, user_password);

    IF role = 'admin' THEN
        EXECUTE format('GRANT ALL PRIVILEGES ON DATABASE smart_home_db TO %I', username);
    ELSIF role = 'guest' THEN
        EXECUTE format('GRANT CONNECT ON DATABASE smart_home_db TO %I', username);
        EXECUTE format('GRANT SELECT ON ALL TABLES IN SCHEMA public TO %I', username);
    END IF;
END;
$$;
