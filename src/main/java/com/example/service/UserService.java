package com.example.service;

import com.example.dao.UserDAO;
import com.example.dao.impl.UserDAOImpl;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public Long createUser(String name, String email, Integer age) {
        logger.debug("Creating user with name: {}, email: {}, age: {}", name, email, age);

        // Check if email already exists
        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = new User(name, email, age);
        return userDAO.create(user);
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("Getting user by ID: {}", id);
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() {
        logger.debug("Getting all users");
        return userDAO.findAll();
    }

    public boolean updateUser(Long id, String name, String email, Integer age) {
        logger.debug("Updating user with ID: {}", id);

        Optional<User> userOptional = userDAO.findById(id);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);

        return userDAO.update(user);
    }

    public boolean deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        return userDAO.delete(id);
    }

    public Optional<User> getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);
        return userDAO.findByEmail(email);
    }
}