package com.example.databaseapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static com.example.databaseapp.FileDatabasePostgres.getAvailableDatabases;

public class Controller {
    @FXML private TableView<Device> deviceTable;
    @FXML private TableColumn<Device, Integer> idColumn;
    @FXML private TableColumn<Device, String> nameColumn;
    @FXML private TableColumn<Device, String> typeColumn;
    @FXML private TableColumn<Device, String> statusColumn;

    @FXML private TextField deviceNameField;
    @FXML private TextField deviceTypeField;
    @FXML private TextField deviceStatusField;

    @FXML private TextField searchNameField;
    @FXML private TextField searchTypeField;
    @FXML private TextField searchStatusField;

    private FileDatabasePostgres database = new FileDatabasePostgres();
    private ObservableList<Device> devices = FXCollections.observableArrayList();

    public Controller() throws SQLException, IOException, ClassNotFoundException {
    }

    @FXML
    public void initialize() throws SQLException {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        deviceTable.setItems(devices);
        loadDevices();
    }

    private void loadDevices() throws SQLException {
        List<Device> deviceList = database.getAllDevices();
        devices.setAll(deviceList);
    }

    @FXML
    private void onAddDeviceClick(ActionEvent event) throws SQLException {
        String name = deviceNameField.getText();
        String type = deviceTypeField.getText();
        String status = deviceStatusField.getText();
        if (!name.isEmpty() && !type.isEmpty() && !status.isEmpty()) {
            database.addDevice(new Device(0, name, type, status));
            loadDevices();
        }
    }

    @FXML
    private void onEditDeviceClick(ActionEvent event) throws SQLException {
        Device selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setName(deviceNameField.getText());
            selected.setType(deviceTypeField.getText());
            selected.setStatus(deviceStatusField.getText());
            database.updateDevice(selected);
            loadDevices();
        }
    }

    @FXML
    private void onDeleteDeviceClick() throws SQLException {
        Device selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            database.deleteDevice(selected.getId());
            loadDevices();
        }
    }

    @FXML
    private void onSearchDeviceClick() throws SQLException {
        String name = searchNameField.getText();
        String type = searchTypeField.getText();
        String status = searchStatusField.getText();
        devices.setAll(database.searchDevices(name, type, status));
    }

    @FXML
    private void onExportToExcelClick(ActionEvent event) {
        // Добавить экспорт в Excel
    }

    @FXML
    private void onUpdateDatabaseClick(ActionEvent event) throws SQLException {
        loadDevices();
    }

    @FXML
    private void onClearDatabaseClick(ActionEvent event) throws SQLException {
        database.clearAllDevices();
        loadDevices();
    }

    public void setPrimaryStage(Stage stage) {
    }

    @FXML
    public void onSelectDatabaseClick(ActionEvent actionEvent) {
        try {
            // Получаем список баз данных
            List<String> databases = getAvailableDatabases();

            if (databases.isEmpty()) {
                showAlert("Нет доступных баз данных.");
                return;
            }

            // Создаём диалог выбора базы
            ChoiceDialog<String> dialog = new ChoiceDialog<>(databases.get(0), databases);
            dialog.setTitle("Выбор базы данных");
            dialog.setHeaderText("Выберите базу данных для подключения");
            dialog.setContentText("Доступные БД:");

            // Ожидаем выбор пользователя
            dialog.showAndWait().ifPresent(selectedDb -> {
                try {
                    database.connectToDatabase(selectedDb);
                    showAlert("Подключено к базе: " + selectedDb);
                } catch (SQLException e) {
                    showAlert("Ошибка подключения: " + e.getMessage());
                }
            });

        } catch (SQLException e) {
            showAlert("Ошибка получения списка БД: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void onCreateDatabaseClick(ActionEvent actionEvent) {
    }

    public void onEditButtonClick(ActionEvent actionEvent) {
    }

    public void onAddButtonClick(ActionEvent actionEvent) {
    }

    public void onDeleteButtonClick(ActionEvent actionEvent) {
    }

    public void onFindButtonClick(ActionEvent actionEvent) {
    }

    public void onResetButtonClick(ActionEvent actionEvent) {
    }

    public void onUpdateButtonClick(ActionEvent actionEvent) {
    }

    public void onClearButtonClick(ActionEvent actionEvent) {
    }
}
