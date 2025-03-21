UPDATE pg_database SET encoding = pg_char_to_encoding('UTF8');

-- Создание базы данных и таблицы devices
CREATE OR REPLACE FUNCTION create_database(db_name TEXT) RETURNS VOID AS $$
DECLARE
    sql_command TEXT;
BEGIN
    -- Проверяем, существует ли уже база
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = db_name) THEN
        -- Создаём базу данных
        sql_command := format('CREATE DATABASE %I', db_name);
        EXECUTE sql_command;


    END IF;

    -- Выдаем права на подключение к новой базе данных всем пользователям
    sql_command := format('GRANT CONNECT ON DATABASE %I TO PUBLIC', db_name);
    EXECUTE sql_command;
    sql_command := format('GRANT SELECT ON ALL TABLES IN SCHEMA public TO PUBLIC', db_name);
    EXECUTE sql_command;

    -- Подключаемся к новой базе и создаём таблицу devices
    sql_command := format(
        'CREATE TABLE IF NOT EXISTS %I.devices (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            type VARCHAR(50),
            status BOOLEAN DEFAULT false
        );', db_name);
    EXECUTE sql_command;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION get_users()
RETURNS TABLE (username TEXT, is_admin BOOLEAN) AS $$
BEGIN
    RETURN QUERY
    SELECT
        rolname::TEXT AS username,
        rolsuper AS is_admin -- rolsuper = TRUE, если пользователь является администратором
    FROM pg_roles
    WHERE rolcanlogin = TRUE; -- Возвращаем только роли, которые могут логиниться
END;
$$ LANGUAGE plpgsql;

-- Хранимая процедура для создания таблицы в текущей БД
CREATE OR REPLACE PROCEDURE create_devices_table()
LANGUAGE plpgsql AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS devices (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        type VARCHAR(50),
        status BOOLEAN DEFAULT false
    );
END;
$$;

CREATE OR REPLACE PROCEDURE ensure_devices_table_exists()
LANGUAGE plpgsql AS $$
BEGIN
    -- Проверяем и создаём таблицу, если её нет
    CREATE TABLE IF NOT EXISTS devices (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        type VARCHAR(50),
        status BOOLEAN DEFAULT false
    );
END;
$$;

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

-- Создаем роль, если она не существует
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'connect_role') THEN
        CREATE ROLE connect_role;
    END IF;
END $$;

-- Выдаем права на подключение ко всем базам данных
DO $$
DECLARE
    dbname TEXT;
BEGIN
    -- Перебираем все базы данных
    FOR dbname IN SELECT datname FROM pg_database LOOP
        -- Проверяем, не выданы ли уже права на подключение
        IF NOT EXISTS (
            SELECT 1
            FROM pg_database
            JOIN pg_roles ON pg_roles.rolname = 'connect_role'
            WHERE datname = dbname
              AND has_database_privilege('connect_role', dbname, 'CONNECT')
        ) THEN
            EXECUTE format('GRANT CONNECT ON DATABASE %I TO connect_role', dbname);
        END IF;
    END LOOP;
END $$;

CREATE OR REPLACE PROCEDURE create_user(username VARCHAR, user_password TEXT, role VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    -- Создаем пользователя
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', username, user_password);

    -- Даем права в зависимости от роли
    IF role = 'admin' THEN
        -- Администратор получает полный доступ
        EXECUTE format('GRANT ALL PRIVILEGES ON DATABASE smart_home_db TO %I', username);
        EXECUTE format('GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO %I', username);
        EXECUTE format('GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO %I', username);

        -- Даем права на будущие таблицы и последовательности
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO %I', username);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO %I', username);
    ELSIF role = 'user' THEN
        -- Обычный пользователь получает доступ только на чтение
        EXECUTE format('GRANT CONNECT ON DATABASE smart_home_db TO %I', username);
        EXECUTE format('GRANT SELECT ON ALL TABLES IN SCHEMA public TO %I', username);

        -- Даем права на чтение будущих таблиц
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO %I', username);

        -- Даем право на подключение ко всем базам данных
        EXECUTE format('GRANT connect_role TO %I', username);
    END IF;
END;
$$;

-- Функция для получения всех устройств
CREATE OR REPLACE FUNCTION get_all_devices()
RETURNS TABLE(id INT, name VARCHAR, type VARCHAR, status BOOLEAN) AS $$
BEGIN
    RETURN QUERY SELECT * FROM devices;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION search_devices(device_name VARCHAR, device_type VARCHAR, device_status VARCHAR)
RETURNS TABLE(id INT, name VARCHAR, type VARCHAR, status BOOLEAN) AS $$
BEGIN
    RETURN QUERY
    SELECT devices.id, devices.name, devices.type, devices.status
    FROM devices
    WHERE
        (device_name IS NULL OR devices.name ILIKE '%' || device_name || '%') AND
        (device_type IS NULL OR devices.type ILIKE '%' || device_type || '%') AND
        (device_status IS NULL OR devices.status::VARCHAR ILIKE '%' || device_status || '%');
END;
$$ LANGUAGE plpgsql;

-- Процедура для удаления устройства по ID
CREATE OR REPLACE PROCEDURE delete_device_by_id(device_id INT)
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM devices WHERE id = device_id;
END;
$$;

-- Процедура для обновления устройства
CREATE OR REPLACE PROCEDURE update_device(device_id INT, device_name VARCHAR, device_type VARCHAR, device_status BOOLEAN)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE devices SET name = device_name, type = device_type, status = device_status WHERE id = device_id;
END;
$$;

-- Процедура для очистки всех устройств
CREATE OR REPLACE PROCEDURE clear_all_devices()
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM devices;
END;
$$;