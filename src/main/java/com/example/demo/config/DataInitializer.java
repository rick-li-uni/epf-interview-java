package com.example.demo.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataInitializer {
    private static final Random random = new Random();

    public static void initializeData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
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
                System.out.println("Tables might not exist yet, continuing...");
                conn.rollback();
            }

            // Insert users
            System.out.println("Starting user insertion...");
            String insertUserSQL = "INSERT INTO users (name, email, is_seller) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
                for (int i = 1; i <= 10000; i++) {
                    pstmt.setString(1, "User" + i);
                    pstmt.setString(2, "user" + i + "@example.com");
                    pstmt.setBoolean(3, random.nextBoolean());
                    pstmt.addBatch();

                    if (i % 1000 == 0) {
                        pstmt.executeBatch();
                        conn.commit();
                        System.out.println("Inserted " + i + " users");
                    }
                }
            }

            // Get seller IDs
            List<Long> sellerIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE is_seller = true")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    sellerIds.add(rs.getLong("id"));
                }
            }

            if (sellerIds.isEmpty()) {
                System.out.println("No sellers found, creating a default seller");
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (name, email, is_seller) VALUES (?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
                )) {
                    stmt.setString(1, "Default Seller");
                    stmt.setString(2, "seller@example.com");
                    stmt.setBoolean(3, true);
                    stmt.executeUpdate();
                    
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        sellerIds.add(rs.getLong(1));
                    }
                }
                conn.commit();
            }

            // Insert products
            System.out.println("Starting product insertion...");
            String insertProductSQL = 
                "INSERT INTO products (name, description, price, seller_id, quantity) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertProductSQL)) {
                for (int i = 1; i <= 100000; i++) {
                    Long sellerId = sellerIds.get(random.nextInt(sellerIds.size()));
                    pstmt.setString(1, "Product" + i);
                    pstmt.setString(2, "Description for product " + i);
                    pstmt.setDouble(3, 20.0 + random.nextDouble() * 980.0);
                    pstmt.setLong(4, sellerId);
                    pstmt.setInt(5, random.nextInt(1000));
                    pstmt.addBatch();

                    if (i % 1000 == 0) {
                        pstmt.executeBatch();
                        conn.commit();
                        System.out.println("Inserted " + i + " products");
                    }
                }
            }

            System.out.println("Data initialization completed!");
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing data", e);
        }
    }
} 