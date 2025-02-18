package com.example.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static Properties properties = new Properties();
    private static final String PROPERTIES_PATH = "file:/Users/kl68884/projects/interviews/epf-interview/src/main/resources/database.properties";
    
    static {
        loadProperties();
    }

    private static void loadProperties() {
        try {
            URL url = new URL(PROPERTIES_PATH);
            try (InputStream input = url.openStream()) {
                properties.load(input);
                // Load the JDBC Driver
                Class.forName(properties.getProperty("db.driver"));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading database properties");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            properties.getProperty("db.url"),
            properties.getProperty("db.username"),
            properties.getProperty("db.password")
        );
    }
} 