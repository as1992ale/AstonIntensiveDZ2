package com.example;

import com.example.dao.UserDAO;
import com.example.dao.impl.UserDAOImpl;
import com.example.model.User;
import com.example.service.UserService;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("integration")
class UserServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("integrationdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    private UserService userService;
    private SessionFactory sessionFactory;
    private UserDAO userDAO;

    @BeforeEach
    void setup() {
        sessionFactory = TestHibernateUtil.getSessionFactory(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        );
        userDAO = new UserDAOImpl(sessionFactory);
        userService = new UserService(userDAO);
    }

    @AfterEach
    void tearDown() {
        // Очищаем базу после каждого теста
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.createQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        }
        TestHibernateUtil.shutdown();
    }

    @Test
    void fullCrudIntegrationTest() {
        // Create
        Long userId = userService.createUser("Integration User", "integration@example.com", 25);
        assertNotNull(userId);

        // Read
        Optional<User> createdUser = userService.getUserById(userId);
        assertTrue(createdUser.isPresent());
        assertEquals("Integration User", createdUser.get().getName());
        assertEquals("integration@example.com", createdUser.get().getEmail());

        // Read All
        List<User> allUsers = userService.getAllUsers();
        assertEquals(1, allUsers.size());

        // Update
        boolean updated = userService.updateUser(userId, "Updated User", "updated@example.com", 30);
        assertTrue(updated);

        Optional<User> updatedUser = userService.getUserById(userId);
        assertTrue(updatedUser.isPresent());
        assertEquals("Updated User", updatedUser.get().getName());
        assertEquals("updated@example.com", updatedUser.get().getEmail());
        assertEquals(30, updatedUser.get().getAge());

        // Find by Email
        Optional<User> byEmail = userService.getUserByEmail("updated@example.com");
        assertTrue(byEmail.isPresent());
        assertEquals(userId, byEmail.get().getId());

        // Delete
        boolean deleted = userService.deleteUser(userId);
        assertTrue(deleted);

        // Verify deletion
        Optional<User> deletedUser = userService.getUserById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void createUser_shouldThrowExceptionForDuplicateEmail() {
        userService.createUser("User1", "duplicate@example.com", 20);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("User2", "duplicate@example.com", 30)
        );

        assertEquals("User with email duplicate@example.com already exists", exception.getMessage());
    }

    @Test
    void updateNonExistentUser_shouldReturnFalse() {
        boolean result = userService.updateUser(999L, "Non-existent", "none@example.com", 25);
        assertFalse(result);
    }

    @Test
    void deleteNonExistentUser_shouldReturnFalse() {
        boolean result = userService.deleteUser(999L);
        assertFalse(result);
    }
}