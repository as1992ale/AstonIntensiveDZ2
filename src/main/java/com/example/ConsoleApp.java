package com.example;

import com.example.model.User;
import com.example.service.UserService;
import com.example.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleApp.class);
    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Starting User Service Console Application");

        try {
            displayMenu();
            boolean running = true;

            while (running) {
                System.out.print("\nEnter your choice (1-6): ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        getUserById();
                        break;
                    case "3":
                        getAllUsers();
                        break;
                    case "4":
                        updateUser();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        running = false;
                        System.out.println("Exiting application...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }

                if (running) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    displayMenu();
                }
            }

        } catch (Exception e) {
            logger.error("Application error: {}", e.getMessage(), e);
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
            logger.info("Application terminated");
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== User Service Management System ===");
        System.out.println("1. Create User");
        System.out.println("2. Get User by ID");
        System.out.println("3. Get All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("6. Exit");
        System.out.println("======================================");
    }

    private static void createUser() {
        System.out.println("\n=== Create New User ===");

        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            System.out.print("Enter age: ");
            String ageInput = scanner.nextLine();
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            Long id = userService.createUser(name, email, age);
            System.out.println("User created successfully with ID: " + id);

        } catch (NumberFormatException e) {
            System.out.println("Invalid age format. Please enter a valid number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Failed to create user: " + e.getMessage());
        }
    }

    private static void getUserById() {
        System.out.println("\n=== Get User by ID ===");

        try {
            System.out.print("Enter user ID: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                System.out.println("User found: " + userOptional.get());
            } else {
                System.out.println("User with ID " + id + " not found");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getAllUsers() {
        System.out.println("\n=== All Users ===");

        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found");
            } else {
                users.forEach(user -> System.out.println(user));
            }

        } catch (Exception e) {
            System.out.println("Failed to retrieve users: " + e.getMessage());
        }
    }

    private static void updateUser() {
        System.out.println("\n=== Update User ===");

        try {
            System.out.print("Enter user ID to update: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                System.out.println("User with ID " + id + " not found");
                return;
            }

            User user = existingUser.get();
            System.out.println("Current user: " + user);

            System.out.print("Enter new name (press Enter to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                user.setName(name);
            }

            System.out.print("Enter new email (press Enter to keep current): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) {
                user.setEmail(email);
            }

            System.out.print("Enter new age (press Enter to keep current): ");
            String ageInput = scanner.nextLine();
            if (!ageInput.isEmpty()) {
                user.setAge(Integer.parseInt(ageInput));
            }

            boolean updated = userService.updateUser(id, user.getName(), user.getEmail(), user.getAge());
            if (updated) {
                System.out.println("User updated successfully");
            } else {
                System.out.println("Failed to update user");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Please enter valid numbers.");
        } catch (Exception e) {
            System.out.println("Failed to update user: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.println("\n=== Delete User ===");

        try {
            System.out.print("Enter user ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Are you sure you want to delete user with ID " + id + "? (y/n): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("y")) {
                boolean deleted = userService.deleteUser(id);
                if (deleted) {
                    System.out.println("User deleted successfully");
                } else {
                    System.out.println("User with ID " + id + " not found");
                }
            } else {
                System.out.println("Deletion cancelled");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Failed to delete user: " + e.getMessage());
        }
    }
}