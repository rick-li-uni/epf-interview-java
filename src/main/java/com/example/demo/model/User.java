package com.example.demo.model;

public class User {
    private Long id;
    private String name;
    private String email;
    private Boolean isSeller;

    // Constructors
    public User() {}

    public User(Long id, String name, String email, Boolean isSeller) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isSeller = isSeller;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsSeller() {
        return isSeller;
    }

    public void setIsSeller(Boolean isSeller) {
        this.isSeller = isSeller;
    }
} 