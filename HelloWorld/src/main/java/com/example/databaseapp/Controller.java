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
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        deviceTable.setItems(devices);
        loadDevices();
    }

    private void loadDevices() {
        List<Device> deviceList = database.getAllDevices();
        devices.setAll(deviceList);
    }

    @FXML
    private void onAddDeviceClick(ActionEvent event) {
        String name = deviceNameField.getText();
        String type = deviceTypeField.getText();
        String status = deviceStatusField.getText();
        if (!name.isEmpty() && !type.isEmpty() && !status.isEmpty()) {
            database.addDevice(new Device(0, name, type, status));
            loadDevices();
        }
    }

    @FXML
    private void onEditDeviceClick(ActionEvent event) {
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
    private void onDeleteDeviceClick() {
        Device selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            database.deleteDevice(selected.getId());
            loadDevices();
        }
    }

    @FXML
    private void onSearchDeviceClick() {
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
    private void onUpdateDatabaseClick(ActionEvent event) {
        loadDevices();
    }

    @FXML
    private void onClearDatabaseClick(ActionEvent event) {
        database.clearAllDevices();
        loadDevices();
    }

    public void setPrimaryStage(Stage stage) {
    }

    public void onSelectDatabaseClick(ActionEvent actionEvent) {
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
