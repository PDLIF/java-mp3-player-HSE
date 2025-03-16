package com.example.databaseapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class Controller {
    public TextField FindId;
    public TextField FindName;
    public DatePicker FindBirthdate;
    public TextField FindEmail;
    public HBox SearchButtons;
    public HBox TableButtons;
    public HBox DataBaseActiveButtons;
    @FXML private TextField nameField;
    @FXML private DatePicker birthdatePicker;
    @FXML private TextField emailField;
    @FXML private TableView<Person> dataTable;
    @FXML private TableColumn<Person, Integer> idColumn;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> birthdateColumn;
    @FXML private TableColumn<Person, String> emailColumn;

    private FileDatabasePostgres database = new FileDatabasePostgres();
    private String backupFilePath;
    private ObservableList<Person> data = FXCollections.observableArrayList();

    public Controller() throws IOException, SQLException, ClassNotFoundException {
    }

    @FXML
    public void initialize() {


        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        birthdateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBirthdate()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));

        dataTable.setItems(data);

        updateUIState(false);
        //loadData();
    }

    private void loadData() {
//        List<Person> people = database.readAll();
//        data.setAll(people);
//        dataTable.setItems(data); // Обновляем таблицу
    }

    @FXML
    private void onAddButtonClick(ActionEvent event) {

    }

    @FXML
    private void onSelectDatabaseClick(ActionEvent event) {

    }
    @FXML
    private void onCreateDatabaseClick(ActionEvent event) throws SQLException, IOException, ClassNotFoundException {

    }


    @FXML
    private void onResetButtonClick(ActionEvent event) {

    }
    @FXML
    public void onClearButtonClick(ActionEvent actionEvent) throws IOException {

    }
    @FXML
    private void onExportToExcelClick(ActionEvent event) {

    }

    // Блок второго ряда

    @FXML
    private void onUpdateButtonClick(ActionEvent actionEvent) {

    }
    @FXML
    private void onDeleteButtonClick() {

    }
    @FXML
    private void onEditButtonClick(ActionEvent event) {

    }
    @FXML
    private void onFindButtonClick(ActionEvent event) {

    }

    // Служебные функции
    private void refreshTable() throws IOException {

    }
    
    private Person getSelectedPerson() {
        return null;
    }

    private void showAlert(String message) {

    }

    private void updateUIState(boolean isDatabaseSelected) {

    }

    private Stage primaryStage; // Поле для хранения ссылки на основное окно
    // Метод для установки основного окна
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        updateAppTitle(); // Обновляем заголовок при инициализации
    }

    private void updateAppTitle() {

    }

    // Метод для генерации уникального ID
    private int generateUniqueId() {

        return 0;
    }

}
