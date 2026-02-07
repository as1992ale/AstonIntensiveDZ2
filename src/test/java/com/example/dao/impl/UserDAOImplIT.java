package com.example.dao.impl;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.TestHibernateUtil;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;  // ← маленькая 'c'

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;  // ← без @

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("integration")
class UserDAOImplIT {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    private UserDAO userDAO;
    private SessionFactory sessionFactory;

    @BeforeEach
    void setup() {
        sessionFactory = TestHibernateUtil.getSessionFactory(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        );
        userDAO = new UserDAOImpl(sessionFactory);
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
    void createUser_shouldReturnId() {
        User user = new User("Test User", "test@example.com", 25);

        Long id = userDAO.create(user);

        assertNotNull(id);
        assertTrue(id > 0);
    }


}