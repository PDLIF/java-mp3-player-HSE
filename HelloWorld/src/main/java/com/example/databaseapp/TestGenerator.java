package com.example.databaseapp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class TestGenerator {

    public static void generateDeviceData(FileDatabasePostgres databasePostgres, int numberOfRecords) throws SQLException {
        Random random = new Random();
        String[] names = {"Smart Light", "Thermostat", "Security Camera", "Smart Plug", "Smart Lock", "Motion Sensor", "Doorbell", "Smart Speaker", "Smart TV", "Smart Fridge"};
        String[] types = {"Lighting", "Climate", "Security", "Energy", "Access", "Sensor", "Audio", "Video", "Appliance"};


            for (int i = 1; i <= numberOfRecords; i++) {
                String name = names[random.nextInt(names.length)];
                String type = types[random.nextInt(types.length)];
                boolean status = random.nextBoolean();

                Device device = new Device(i,name,type,status);
                databasePostgres.addDevice(device);

            }
            System.out.println("Данные устройств успешно сгенерированы");

    }
}