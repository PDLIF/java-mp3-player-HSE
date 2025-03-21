package com.example.databaseapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.incubator.vector.VectorOperators;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    private FileDatabasePostgres database;
    private ObservableList<Device> devices = FXCollections.observableArrayList();
    private Stage stage;
    public Controller() throws SQLException, IOException, ClassNotFoundException {
    }

    // Init
    @FXML
    public void initialize() throws SQLException, IOException, ClassNotFoundException {
        database = new FileDatabasePostgres();


        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        deviceTable.setItems(devices);

    }

    public void setPrimaryStage(Stage stage) {
        this.stage = stage;
    }


    // Top
    @FXML
    public void onSelectDatabaseClick(ActionEvent actionEvent) throws SQLException {
        try {
            // Используем массив для обхода ограничения на effectively final
            List<String>[] databasesHolder = new List[]{getAvailableDatabases()};
            databasesHolder[0].add("Создать новую базу данных...");

            // Создаем диалог выбора базы данных
            ChoiceDialog<String> dialog = new ChoiceDialog<>(databasesHolder[0].get(0), databasesHolder[0]);
            dialog.setTitle("Выбор базы данных");
            dialog.setHeaderText("Выберите базу данных или создайте новую");
            dialog.setContentText("Доступные БД:");

            // Добавляем кнопку "Удалить БД"
            ButtonType deleteButtonType = new ButtonType("Удалить БД", ButtonBar.ButtonData.OTHER);
            dialog.getDialogPane().getButtonTypes().add(deleteButtonType);

            // Обработка нажатия кнопки "Удалить БД"
            Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
            deleteButton.setOnAction(event -> {
                String selectedDb = dialog.getSelectedItem();
                if (selectedDb != null && !selectedDb.equals("Создать новую базу данных...")) {
                    handleDeleteDatabase(selectedDb);
                    // Обновляем список баз данных после удаления
                    try {
                        databasesHolder[0] = getAvailableDatabases();
                        databasesHolder[0].add("Создать новую базу данных...");
                        dialog.getItems().setAll(databasesHolder[0]);
                    } catch (SQLException e) {
                        showAlert("Ошибка обновления списка БД: " + e.getMessage());
                    }
                } else {
                    showAlert("Нельзя удалить выбранный элемент!");
                }
            });

            // Обработка нажатия кнопки "ОК"
            dialog.showAndWait().ifPresent(selectedDb -> {
                if ("Создать новую базу данных...".equals(selectedDb)) {
                    handleCreateDatabase();
                } else {
                    handleSelectDatabase(selectedDb);
                }
            });

        } catch (SQLException e) {
            showAlert("Ошибка получения списка БД: " + e.getMessage());
        }

        loadDevices();
    }

    // Функция для создания новой базы данных
    private void handleCreateDatabase() {
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
                stage.setTitle("Работа с БД: "+newDbName);
            } catch (SQLException | IOException e) {
                showAlert("Ошибка при создании БД: " + e.getMessage());
            }
        });
    }

    // Функция для выбора базы данных
    private void handleSelectDatabase(String selectedDb) {
        try {
            database.connectToDatabase(selectedDb);
            showAlert("Подключено к базе: " + selectedDb);
            stage.setTitle("Работа с БД: "+selectedDb);
        } catch (SQLException e) {
            showAlert("Ошибка подключения: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDeleteDatabase(String dbName) {
        Platform.runLater(() -> {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.initModality(Modality.APPLICATION_MODAL); // Делаем диалог модальным
            confirmationDialog.setTitle("Удаление базы данных");
            confirmationDialog.setHeaderText("Вы уверены, что хотите удалить базу данных?");
            confirmationDialog.setContentText("База данных: " + dbName);
            confirmationDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO); // Убедимся, что кнопки добавлены
            confirmationDialog.getDialogPane().getStylesheets().clear(); // Очищаем стили, если они есть

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                try {
                    if (dbName == null || dbName.trim().isEmpty()) {
                        showAlert("Имя базы данных не может быть пустым!");
                        return;
                    }
                    database.DeleteDatabase(dbName);
                    showAlert("База данных '" + dbName + "' успешно удалена!");
                } catch (SQLException | IOException e) {
                    showAlert("Ошибка при удалении БД: " + e.getMessage());
                }
            }
        });
    }

    private void loadDevices() throws SQLException {
        List<Device> deviceList = database.getAllDevices();
        devices.setAll(deviceList);
    }

    @FXML
    private void onSelectUser(ActionEvent event) {
        try {
            // Получаем список пользователей из базы данных
            List<String> users = database.getUsers();
            users.add("Создать нового"); // Добавляем опцию создания нового пользователя

            // Диалог выбора пользователя
            ChoiceDialog<String> userDialog = new ChoiceDialog<>(users.get(0), users);
            userDialog.setTitle("Выбор пользователя");
            userDialog.setHeaderText("Выберите пользователя или создайте нового");
            userDialog.setContentText("Выберите пользователя:");

            userDialog.showAndWait().ifPresent(selectedUser -> {
                if ("Создать нового".equals(selectedUser)) {
                    // Логика создания нового пользователя
                    createNewUser();
                } else {
                    // Обработка выбора существующего пользователя
                    showAlert("Выбран существующий пользователь: " + selectedUser);
                }
            });
        } catch (SQLException e) {
            showAlert("Ошибка при получении списка пользователей: " + e.getMessage());
        }
    }

    /**
     * Создание нового пользователя.
     */
    private void createNewUser() {
        // Диалог для ввода имени нового пользователя
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Создание пользователя");
        usernameDialog.setHeaderText("Введите имя нового пользователя");
        usernameDialog.setContentText("Имя пользователя:");

        usernameDialog.showAndWait().ifPresent(newUsername -> {
            if (newUsername.trim().isEmpty()) {
                showAlert("Имя пользователя не может быть пустым!");
                return;
            }

            // Диалог для выбора роли нового пользователя
            List<String> availableRoles = Arrays.asList("Админ", "Пользователь");
            ChoiceDialog<String> roleSelection = new ChoiceDialog<>(availableRoles.get(0), availableRoles);
            roleSelection.setTitle("Выбор роли");
            roleSelection.setHeaderText("Выберите роль нового пользователя");
            roleSelection.setContentText("Роль:");

            roleSelection.showAndWait().ifPresent(newRole -> {
                // Диалог для ввода пароля нового пользователя
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
                        // Создание пользователя в базе данных
                        database.createUser(newUsername, newPassword, newRole);
                        showAlert("Пользователь '" + newUsername + "' с ролью '" + newRole + "' успешно создан!");

                        // Обновление списка пользователей
                        onSelectUser(null); // Перезагружаем диалог
                    } catch (SQLException e) {
                        showAlert("Ошибка при создании пользователя: " + e.getMessage());
                    }
                });
            });
        });
    }

    @FXML
    private void onClearDatabaseClick(ActionEvent event) throws SQLException {
        database.clearAllDevices();
        loadDevices();
    }

    @FXML
    private void onFillDatabaseClick(ActionEvent event) throws SQLException {
        TestGenerator.generateDeviceData(database,1000);
        loadDevices();
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
