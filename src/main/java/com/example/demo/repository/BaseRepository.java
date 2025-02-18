package com.example.demo.repository;

import java.sql.Connection;
import java.sql.SQLException;

import com.example.demo.config.DatabaseConfig;

public abstract class BaseRepository {
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
} 