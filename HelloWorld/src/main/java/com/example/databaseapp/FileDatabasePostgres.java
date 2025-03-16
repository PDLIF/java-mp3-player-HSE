package com.example.databaseapp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class FileDatabasePostgres {
    private Connection conn;

    public FileDatabasePostgres() throws ClassNotFoundException, SQLException, IOException {
        String dbName = "smart_home_db";
        String url = "jdbc:postgresql://127.0.0.1:5432/";
        String userName = "postgres", userPassd = "123";

        Class.forName("org.postgresql.Driver");

        // Подключаемся к серверу PostgreSQL без указания БД
        try (Connection tempConn = DriverManager.getConnection(url, userName, userPassd);
             Statement stmt = tempConn.createStatement()) {

            // Проверяем, существует ли база данных
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
            if (!rs.next()) {
                // Если БД нет — создаём
                stmt.executeUpdate("CREATE DATABASE " + dbName);
                System.out.println("База данных " + dbName + " создана.");
            }
        }

        // Подключаемся к созданной базе
        this.conn = DriverManager.getConnection(url + dbName, userName, userPassd);
        System.out.println("Соединение установлено");

        // Загружаем SQL-скрипты при первом запуске
        executeSQLFile("scripts.sql");
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

    // Вызов процедуры создания БД
    public void createDatabase() throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL create_database()")) {
            stmt.execute();
        }
    }

    // Вызов процедуры удаления БД
    public void dropDatabase() throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL drop_database()")) {
            stmt.execute();
        }
    }

    // Очистка таблицы устройств
    public void clearDevicesTable() throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL clear_devices_table()")) {
            stmt.execute();
        }
    }

    // Добавление нового устройства
    public void insertDevice(String name, String type, boolean status) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL insert_device(?, ?, ?)");) {
            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setBoolean(3, status);
            stmt.execute();
        }
    }

    // Поиск устройства по названию
    public void searchDevice(String name) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("SELECT * FROM search_device(?)")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Type: " + rs.getString("type") + ", Status: " + rs.getBoolean("status"));
            }
        }
    }

    // Обновление статуса устройства
    public void updateDeviceStatus(int deviceId, boolean newStatus) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL update_device_status(?, ?)");) {
            stmt.setInt(1, deviceId);
            stmt.setBoolean(2, newStatus);
            stmt.execute();
        }
    }

    // Удаление устройства по названию
    public void deleteDeviceByName(String name) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL delete_device_by_name(?)")) {
            stmt.setString(1, name);
            stmt.execute();
        }
    }

    // Создание пользователя с правами доступа
    public void createUser(String username, String password, String role) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall("CALL create_user(?, ?, ?)");) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.execute();
        }
    }

    public static void main(String[] args) {
        try {
            FileDatabasePostgres db = new FileDatabasePostgres();
            db.createDatabase();
            db.insertDevice("Smart Light", "Light", true);
            db.searchDevice("Smart Light");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDevice(int id) {
    }

    public Device searchDevices(String name, String type, String status) {
        return null;
    }

    public List<Device> getAllDevices() {
        return List.of();
    }

    public void addDevice(Device device) {
    }

    public void updateDevice(Device selected) {
    }

    public void clearAllDevices() {
    }
}
