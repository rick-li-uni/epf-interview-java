package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.config.DataInitializer;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        
        SpringApplication.run(DemoApplication.class, args);
        
        // Initialize data after Spring context is loaded
        DataInitializer.initializeData();
    }
} 