package com.example.databaseapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.example.databaseapp.FileDatabasePostgres.getAvailableDatabases;

public class Controller {

    @FXML private TableView<Device> deviceTable;
    @FXML private TableColumn<Device, Integer> idColumn;
    @FXML private TableColumn<Device, String> nameColumn;
    @FXML private TableColumn<Device, String> typeColumn;
    @FXML private TableColumn<Device, Boolean> statusColumn;

    @FXML private TextField deviceNameField;
    @FXML private TextField deviceTypeField;
    @FXML public CheckBox deviceStatusCheckBox;

    @FXML private TextField searchNameField;
    @FXML private TextField searchTypeField;
    @FXML private TextField searchStatusField;

    private FileDatabasePostgres database = new FileDatabasePostgres();
    private ObservableList<Device> devices = FXCollections.observableArrayList();

    public Controller() throws SQLException, IOException, ClassNotFoundException {
    }

    // Init
    @FXML
    public void initialize() throws SQLException {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        deviceTable.setItems(devices);
        loadDevices();
    }

    public void setPrimaryStage(Stage stage) {
    }


    // Top

    @FXML
    public void onSelectDatabaseClick(ActionEvent actionEvent) throws SQLException {
        try {
            List<String> databases = getAvailableDatabases();
            databases.add("Создать новую базу данных...");

            ChoiceDialog<String> dialog = new ChoiceDialog<>(databases.get(0), databases);
            dialog.setTitle("Выбор базы данных");
            dialog.setHeaderText("Выберите базу данных или создайте новую");
            dialog.setContentText("Доступные БД:");

            dialog.showAndWait().ifPresent(selectedDb -> {
                if ("Создать новую базу данных...".equals(selectedDb)) {
                    TextInputDialog inputDialog = new TextInputDialog();
                    inputDialog.setTitle("Создание базы данных");
                    inputDialog.setHeaderText("Введите имя новой базы данных");
                    inputDialog.setContentText("Имя БД:");

                    inputDialog.showAndWait().ifPresent(newDbName -> {
                        if (newDbName.trim().isEmpty()) {
                            showAlert("Имя базы данных не может быть пустым!");
                            return;
                        }
                        try {
                            database.createDatabase(newDbName);
                            showAlert("База данных '" + newDbName + "' успешно создана!");
                        } catch (SQLException | IOException e) {
                            showAlert("Ошибка при создании БД: " + e.getMessage());
                        }
                    });

                } else {
                    try {
                        database.connectToDatabase(selectedDb);
                        showAlert("Подключено к базе: " + selectedDb);
                    } catch (SQLException e) {
                        showAlert("Ошибка подключения: " + e.getMessage());
                    }
                }
            });

        } catch (SQLException e) {
            showAlert("Ошибка получения списка БД: " + e.getMessage());
        }

        loadDevices();
    }

    private void loadDevices() throws SQLException {
        List<Device> deviceList = database.getAllDevices();
        devices.setAll(deviceList);
    }

    @FXML
    private void onSelectUser(ActionEvent event)  {
        List<String> userRoles = Arrays.asList("Админ", "Пользователь", "Создать нового");

        ChoiceDialog<String> roleDialog = new ChoiceDialog<>(userRoles.get(0), userRoles);
        roleDialog.setTitle("Выбор пользователя");
        roleDialog.setHeaderText("Выберите роль пользователя или создайте нового");
        roleDialog.setContentText("Выберите роль:");

        roleDialog.showAndWait().ifPresent(selectedRole -> {
            if ("Создать нового".equals(selectedRole)) {
                TextInputDialog usernameDialog = new TextInputDialog();
                usernameDialog.setTitle("Создание пользователя");
                usernameDialog.setHeaderText("Введите имя нового пользователя");
                usernameDialog.setContentText("Имя пользователя:");

                usernameDialog.showAndWait().ifPresent(newUsername -> {
                    if (newUsername.trim().isEmpty()) {
                        showAlert("Имя пользователя не может быть пустым!");
                        return;
                    }

                    ChoiceDialog<String> roleSelection = new ChoiceDialog<>("Пользователь", "Админ");
                    roleSelection.setTitle("Выбор роли");
                    roleSelection.setHeaderText("Выберите роль нового пользователя");
                    roleSelection.setContentText("Роль:");

                    roleSelection.showAndWait().ifPresent(newRole -> {
                        TextInputDialog passwordDialog = new TextInputDialog();
                        passwordDialog.setTitle("Создание пользователя");
                        passwordDialog.setHeaderText("Введите пароль для пользователя " + newUsername);
                        passwordDialog.setContentText("Пароль:");

                        passwordDialog.showAndWait().ifPresent(newPassword -> {
                            if (newPassword.trim().isEmpty()) {
                                showAlert("Пароль не может быть пустым!");
                                return;
                            }
                            try {
                                database.createUser(newUsername, newPassword, newRole);
                                showAlert("Пользователь '" + newUsername + "' с ролью '" + newRole + "' успешно создан!");
                            } catch (SQLException e) {
                                showAlert("Ошибка при создании пользователя: " + e.getMessage());
                            }
                        });
                    });
                });
            } else {
                showAlert("Выбран существующий пользователь: " + selectedRole);
            }
        });
    }
    @FXML
    private void onClearDatabaseClick(ActionEvent event) throws SQLException {
        database.clearAllDevices();
        loadDevices();
    }

    @FXML
    private void onFillDatabaseClick(ActionEvent event) {
        // Добавить экспорт в Excel
    }




    @FXML
    private void onAddDeviceClick(ActionEvent event) throws SQLException {
        String name = deviceNameField.getText();
        String type = deviceTypeField.getText();
        boolean status = deviceStatusCheckBox.isSelected();
        if (!name.isEmpty() && !type.isEmpty()) {
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
            selected.setStatus(deviceStatusCheckBox.isSelected());
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






    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.out.println(message);
    }



}
