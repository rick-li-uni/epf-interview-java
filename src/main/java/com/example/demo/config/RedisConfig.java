package com.example.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import jakarta.annotation.PreDestroy;

@Configuration
public class RedisConfig {
    private RedisClient redisClient;
    private Properties properties = new Properties();
    private static final String PROPERTIES_PATH = "file:/Users/kl68884/projects/interviews/epf-interview/src/main/resources/database.properties";

    public RedisConfig() {
        loadProperties();
        initializeRedis();
    }

    private void loadProperties() {
        try {
            URL url = new URL(PROPERTIES_PATH);
            try (InputStream input = url.openStream()) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties from " + PROPERTIES_PATH, e);
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