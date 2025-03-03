package com.example.demo.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataInitializer {
    private static final Random random = new Random();

    public static void initializeData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // Print debug information about the database
            System.out.println("Database product name: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Database product version: " + conn.getMetaData().getDatabaseProductVersion());
            
            // Check if tables already exist
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "USERS", null);
            boolean usersTableExists = tables.next();
            System.out.println("USERS table exists: " + usersTableExists);
            
            tables = metaData.getTables(null, null, "PRODUCTS", null);
            boolean productsTableExists = tables.next();
            System.out.println("PRODUCTS table exists: " + productsTableExists);
            
            // Execute schema creation if tables don't exist
            if (!usersTableExists || !productsTableExists) {
                System.out.println("Creating database schema...");
                createSchema(conn);
                System.out.println("Schema created successfully");
            } else {
                System.out.println("Tables already exist, skipping schema creation");
            }
            
            // Clear existing data
            System.out.println("Cleaning existing data...");
            try {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM products")) {
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users")) {
                    stmt.executeUpdate();
                }
                conn.commit();
                System.out.println("Existing data cleaned successfully");
            } catch (SQLException e) {
                System.out.println("Error cleaning existing data: " + e.getMessage());
                conn.rollback();
            }

            // Insert users
            System.out.println("Starting user insertion...");
            String insertUserSQL = "INSERT INTO users (name, email, is_seller) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 1; i <= 100; i++) {
                    pstmt.setString(1, "User" + i);
                    pstmt.setString(2, "user" + i + "@example.com");
                    pstmt.setBoolean(3, random.nextBoolean());
                    pstmt.executeUpdate(); // Execute one at a time instead of batching to get generated keys
                    
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    // For debugging
                    if (i == 1) {
                        System.out.println("Generated keys metadata column count: " + generatedKeys.getMetaData().getColumnCount());
                        for (int j = 1; j <= generatedKeys.getMetaData().getColumnCount(); j++) {
                            System.out.println("Column " + j + " name: " + generatedKeys.getMetaData().getColumnName(j));
                        }
                    }
                    
                    if (i % 20 == 0) {
                        conn.commit();
                        System.out.println("Inserted " + i + " users");
                    }
                }
                conn.commit();
            }

            // Get seller IDs
            List<Integer> sellerIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE is_seller = true")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    sellerIds.add(rs.getInt("id"));
                }
            }

            if (sellerIds.isEmpty()) {
                System.out.println("No sellers found, creating a default seller");
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (name, email, is_seller) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                )) {
                    stmt.setString(1, "Default Seller");
                    stmt.setString(2, "seller@example.com");
                    stmt.setBoolean(3, true);
                    stmt.executeUpdate();
                    
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        sellerIds.add(rs.getInt(1));
                    }
                }
                conn.commit();
            }

            // Insert products
            System.out.println("Starting product insertion...");
            String insertProductSQL = 
                "INSERT INTO products (name, description, price, seller_id, quantity) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertProductSQL)) {
                for (int i = 1; i <= 100; i++) {
                    Integer sellerId = sellerIds.get(random.nextInt(sellerIds.size()));
                    if(i == 1 || i == 2){
                        sellerId = 1; //make sure user 1 has some products
                    }

                    pstmt.setString(1, "Product" + i);
                    pstmt.setString(2, "Description for product " + i);
                    pstmt.setDouble(3, 20.0 + random.nextDouble() * 980.0);
                    pstmt.setInt(4, sellerId);
                    pstmt.setInt(5, random.nextInt(1000));
                    pstmt.executeUpdate();

                    if (i % 20 == 0) {
                        conn.commit();
                        System.out.println("Inserted " + i + " products");
                    }
                }
                conn.commit();
            }

            System.out.println("Data initialization completed!");
        } catch (SQLException e) {
            System.err.println("Error in data initialization: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error initializing data", e);
        }
    }
    
    private static void createSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Drop tables if they exist (to ensure we start clean)
            try {
                stmt.executeUpdate("DROP TABLE products");
                System.out.println("Dropped products table");
            } catch (SQLException e) {
                // Table doesn't exist, ignore
                System.out.println("Products table doesn't exist yet");
            }
            
            try {
                stmt.executeUpdate("DROP TABLE users");
                System.out.println("Dropped users table");
            } catch (SQLException e) {
                // Table doesn't exist, ignore
                System.out.println("Users table doesn't exist yet");
            }
            
            // Users table - using INTEGER for ID
            String createUsersTable = 
                "CREATE TABLE users (" +
                "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL, " +
                "is_seller BOOLEAN DEFAULT FALSE)";
            
            System.out.println("Executing: " + createUsersTable);
            stmt.executeUpdate(createUsersTable);
            
            // Products table - MATCHING INTEGER type for seller_id to match users.id
            String createProductsTable = 
                "CREATE TABLE products (" +
                "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "description LONG VARCHAR, " +
                "price DECIMAL(10,2) NOT NULL, " +
                "seller_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (seller_id) REFERENCES users(id))";
            
            System.out.println("Executing: " + createProductsTable);
            stmt.executeUpdate(createProductsTable);
            
            conn.commit();
            System.out.println("Schema tables created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating schema: " + e.getMessage());
            conn.rollback();
            throw e;
        }
    }
} 