package com.example.service.impl;

import com.example.dto.UserEventDto;
import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;

    private static final String USER_EVENTS_TOPIC = "aston-user-events";

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating user with email: {}", userRequestDto.getEmail());

        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new DataIntegrityViolationException(
                    "User with email " + userRequestDto.getEmail() + " already exists"
            );
        }

        User user = userMapper.toEntity(userRequestDto);
        User savedUser = userRepository.save(user);

        // Отправляем событие в Kafka
        UserEventDto event = UserEventDto.builder()
                .eventType("CREATED")
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .userId(savedUser.getId())
                .build();

        kafkaTemplate.send(USER_EVENTS_TOPIC, event);
        log.info("✅ Sent CREATED event to Kafka for user: {}", savedUser.getEmail());

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(userRequestDto.getEmail()) &&
                userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new DataIntegrityViolationException(
                    "User with email " + userRequestDto.getEmail() + " already exists"
            );
        }

        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());
        user.setAge(userRequestDto.getAge());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        String email = user.getEmail();
        String name = user.getName();

        userRepository.deleteById(id);

        // Отправляем событие в Kafka
        UserEventDto event = UserEventDto.builder()
                .eventType("DELETED")
                .email(email)
                .name(name)
                .userId(id)
                .build();

        kafkaTemplate.send(USER_EVENTS_TOPIC, event);
        log.info("✅ Sent DELETED event to Kafka for user: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return userMapper.toResponseDto(user);
    }
}