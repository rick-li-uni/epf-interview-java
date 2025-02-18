package com.example.demo.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.demo.model.Product;

@Component
public class ProductRepository extends BaseRepository {

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all products", e);
        }
        return products;
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = " + id;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by id: " + id, e);
        }
        return Optional.empty();
    }

    public List<Product> findBySellerId(Long sellerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE seller_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding products by seller id: " + sellerId, e);
        }
        return products;
    }

    public Product save(Product product) {
        String sql = "INSERT INTO products (name, description, price, seller_id, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setLong(4, product.getSellerId());
            stmt.setInt(5, product.getQuantity());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product", e);
        }
        return product;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setSellerId(rs.getLong("seller_id"));
        product.setQuantity(rs.getInt("quantity"));
        return product;
    }
} 