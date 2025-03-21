package com.example.databaseapp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDatabasePostgres {
    private Connection conn;
    static String sql_path = "scripts.sql";
    static String url = "jdbc:postgresql://127.0.0.1:5432/", DataBaseurl = "jdbc:postgresql://127.0.0.1:5432/smart_home_db";
    public static String userName = "postgres", dbName = "smart_home_db";
    static String userPassd = "123", coding = "?charSet=UTF-8";

    public FileDatabasePostgres() throws ClassNotFoundException, SQLException, IOException {
        dbName = "smart_home_db";


        Class.forName("org.postgresql.Driver");

        // Подключаемся к серверу PostgreSQL без указания БД
        try (Connection tempConn = DriverManager.getConnection(url+coding, userName, userPassd);
             Statement stmt = tempConn.createStatement()) {

            // Проверяем, существует ли база данных
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
            if (!rs.next()) {
                // Если БД нет — создаём
                stmt.executeUpdate("CREATE DATABASE " + dbName);
                System.out.println("База данных " + dbName + " создана.");
            }
        }

    }

    public void executeSQLFile(String filePath) throws IOException, SQLException {
        // Загружаем файл из ресурсов (предполагается, что он лежит в src/main/resources)
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        if (is == null) {
            throw new IOException("Файл " + filePath + " не найден в ресурсах.");
        }
        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    private void executeSQLFile(Connection conn, String filePath) throws IOException, SQLException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        if (is == null) {
            throw new IOException("Файл " + filePath + " не найден в ресурсах.");
        }
        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void createDatabase(String dbName) throws SQLException, IOException {
        // 1. Создаём новую БД
        try (Connection adminConn = DriverManager.getConnection(url+"postgres", userName, userPassd);
             Statement stmt = adminConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + dbName);
            conn = adminConn;
        }
        ;
        connectToDatabase(dbName);

    }

    public void deleteDatabase(String dbName) throws SQLException, IOException {
        // Создаем новое соединение с базой данных "postgres" (или другой системной БД)
        try (Connection adminConn = DriverManager.getConnection(url+"postgres", userName, userPassd)) {
            // Формируем SQL-запрос для удаления базы данных
            String sql = "DROP DATABASE " + dbName;

            try (Statement stmt = adminConn.createStatement()) {
                stmt.execute(sql); // Выполняем команду DROP DATABASE
            }
        } catch (SQLException e) {
            throw new SQLException("Ошибка при удалении БД: " + e.getMessage(), e);
        }
    }
    // Добавление нового устройства
    public void addDevice(Device device) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL insert_device(?, ?, ?)")) {
            stmt.setString(1, device.getName());
            stmt.setString(2, device.getType());
            stmt.setBoolean(3, device.getStatus());
            stmt.execute();
        }
    }

    public List<Device> searchDevices(String name, String type, String status) throws SQLException {
        List<Device> devices = new ArrayList<>();
        try (CallableStatement stmt = conn.prepareCall("{ call search_devices(?, ?, ?) }")) {
            // Устанавливаем параметры для поиска
            stmt.setString(1, name.isEmpty() ? null : name); // Если поле пустое, передаем NULL
            stmt.setString(2, type.isEmpty() ? null : type); // Если поле пустое, передаем NULL
            stmt.setString(3, status.isEmpty() ? null : status); // Если поле пустое, передаем NULL

            // Выполняем запрос
            ResultSet rs = stmt.executeQuery();

            // Обрабатываем результат
            while (rs.next()) {
                Device device = new Device(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getBoolean("status")
                );
                devices.add(device);
            }
        }
        return devices;
    }

    // Выбор пользователя
    public void selectUser(String username,String password) throws SQLException {

        userName = username; userPassd = password;


        conn = DriverManager.getConnection(DataBaseurl+coding, userName, userPassd);
    }
    // Создание пользователя
    public void createUser(String username, String password, String role) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL create_user(?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.execute();
        }
        selectUser(username,password);
    }
    public List<User> getUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        // Выполняем SQL-запрос для получения списка пользователей
        try (CallableStatement stmt = conn.prepareCall("{ call get_users() }");
             ResultSet rs = stmt.executeQuery()) {

            // Читаем результаты
            while (rs.next()) {
                String username = rs.getString("username");
                boolean isAdmin = rs.getBoolean("is_admin");
                users.add(new User(username, isAdmin));
            }
        }

        return users;
    }
    // Удаление устройства по ID
    public void deleteDevice(int id) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL delete_device_by_id(?)")) {
            stmt.setInt(1, id);
            stmt.execute();
        }
    }

    // Получение всех устройств
    public List<Device> getAllDevices() throws SQLException {
        List<Device> devices = new ArrayList<>();
        try (CallableStatement stmt = conn.prepareCall("SELECT * FROM get_all_devices()")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Device device = new Device(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getBoolean("status")
                );
                devices.add(device);
            }
        }
        return devices;
    }

    // Обновление устройства
    public void updateDevice(Device device) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL update_device(?, ?, ?, ?)")) {
            stmt.setInt(1, device.getId());
            stmt.setString(2, device.getName());
            stmt.setString(3, device.getType());
            stmt.setBoolean(4, device.getStatus());
            stmt.execute();
        }
    }

    // Очистка всех устройств
    public void clearAllDevices() throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL clear_all_devices()")) {
            stmt.execute();
        }
    }

    // Подключение к базе данных
    public void connectToDatabase(String _dbName) throws SQLException, IOException {
        DataBaseurl = "jdbc:postgresql://127.0.0.1:5432/" + _dbName;
        dbName = _dbName;
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }

        conn = DriverManager.getConnection(DataBaseurl, userName, userPassd);

        executeSQLFile(conn,sql_path);
        try (CallableStatement stmt = conn.prepareCall("CALL create_devices_table()")) {
            stmt.execute();
        }

    }

    // Получение списка доступных баз данных
    public static List<String> getAvailableDatabases() throws SQLException {
        List<String> databases = new ArrayList<>();
        String query = "SELECT datname FROM pg_database WHERE datistemplate = false";

        try (Connection tempConn = DriverManager.getConnection(url, "postgres", "123");
             Statement stmt = tempConn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                databases.add(rs.getString("datname"));
            }
        }
        return databases;
    }

}