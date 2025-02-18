package com.example.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import jakarta.annotation.PreDestroy;

@Configuration
public class RedisConfig {
    private RedisClient redisClient;
    private Properties properties = new Properties();

    public RedisConfig() {
        loadProperties();
        initializeRedis();
    }

    private void loadProperties() {
        try (InputStream input = new ClassPathResource("database.properties").getInputStream()) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties", e);
        }
    }

    private void initializeRedis() {
        try {
            RedisURI redisUri = RedisURI.builder()
                .withHost(properties.getProperty("redis.host"))
                .withPort(Integer.parseInt(properties.getProperty("redis.port")))
                .withAuthentication(
                    properties.getProperty("redis.username"), 
                    properties.getProperty("redis.password")
                )
                .build();

            redisClient = RedisClient.create(redisUri);
            System.out.println("Redis connection established successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize Redis connection: " + e.getMessage());
            throw new RuntimeException("Redis initialization failed", e);
        }
    }

    @Bean
    public RedisClient redisClient() {
        return redisClient;
    }

    @PreDestroy
    public void shutdown() {
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    // Helper method to generate cache key
    public static String generateKey(String prefix, String id) {
        return String.format("%s:%s", prefix, id);
    }
} 