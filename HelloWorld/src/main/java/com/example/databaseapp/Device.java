package com.example.databaseapp;

import javafx.beans.property.*;

public class Device {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty status;

    public Device(int id, String name, String type, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.status = new SimpleStringProperty(status);
    }

    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getType() { return type.get(); }
    public String getStatus() { return status.get(); }

    public void setName(String name) { this.name.set(name); }
    public void setType(String type) { this.type.set(type); }
    public void setStatus(String status) { this.status.set(status); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public StringProperty statusProperty() { return status; }
}
