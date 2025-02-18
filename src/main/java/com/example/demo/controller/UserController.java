package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.RedisConfig;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        connection.sync().set("test", "test");
        return connection.sync().get("test");
        
    }

    @GetMapping("/{id}")
    @ResponseBody
    public User getUserById(@PathVariable("id") Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @PostMapping
    public void createUser(@RequestBody User user) {
        userRepository.save(user);
    }
} 