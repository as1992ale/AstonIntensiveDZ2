package com.example;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", 30);
        testUser.setId(1L);
    }

    @Test
    void createUser_shouldReturnId_whenEmailIsUnique() {
        // Arrange
        when(userDAO.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userDAO.create(any(User.class))).thenReturn(1L);

        // Act
        Long userId = userService.createUser("John Doe", "john@example.com", 30);

        // Assert
        assertEquals(1L, userId);
        verify(userDAO).findByEmail("john@example.com");
        verify(userDAO).create(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange
        when(userDAO.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "existing@example.com", 30)
        );

        assertEquals("User with email existing@example.com already exists", exception.getMessage());
        verify(userDAO).findByEmail("existing@example.com");
        verify(userDAO, never()).create(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        // Arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userDAO).findById(1L);
    }

    @Test
    void getUserById_shouldReturnEmpty_whenNotExists() {
        // Arrange
        when(userDAO.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(userDAO).findById(999L);
    }

    @Test
    void getAllUsers_shouldReturnUserList() {
        // Arrange
        List<User> users = Arrays.asList(
                new User("User1", "user1@example.com", 20),
                new User("User2", "user2@example.com", 30)
        );
        when(userDAO.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        verify(userDAO).findAll();
    }

    @Test
    void updateUser_shouldReturnTrue_whenUserExists() {
        // Arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDAO.update(any(User.class))).thenReturn(true);

        // Act
        boolean result = userService.updateUser(1L, "Updated Name", "updated@example.com", 35);

        // Assert
        assertTrue(result);
        verify(userDAO).findById(1L);
        verify(userDAO).update(argThat(user ->
                user.getName().equals("Updated Name") &&
                        user.getEmail().equals("updated@example.com") &&
                        user.getAge() == 35
        ));
    }

    @Test
    void updateUser_shouldReturnFalse_whenUserNotExists() {
        // Arrange
        when(userDAO.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.updateUser(999L, "New Name", "new@example.com", 25);

        // Assert
        assertFalse(result);
        verify(userDAO).findById(999L);
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void deleteUser_shouldReturnTrue_whenUserExists() {
        // Arrange
        when(userDAO.delete(1L)).thenReturn(true);

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertTrue(result);
        verify(userDAO).delete(1L);
    }

    @Test
    void deleteUser_shouldReturnFalse_whenUserNotExists() {
        // Arrange
        when(userDAO.delete(999L)).thenReturn(false);

        // Act
        boolean result = userService.deleteUser(999L);

        // Assert
        assertFalse(result);
        verify(userDAO).delete(999L);
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        // Arrange
        when(userDAO.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByEmail("john@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
        verify(userDAO).findByEmail("john@example.com");
    }
}