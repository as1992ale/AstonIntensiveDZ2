package com.example.controller;

import com.example.dto.UserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь создан",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "409", description = "Email уже существует")
    })
    public ResponseEntity<EntityModel<UserResponseDto>> createUser(
            @Valid @RequestBody
            @Parameter(description = "Данные для создания пользователя")
            UserRequestDto userRequestDto) {

        UserResponseDto createdUser = userService.createUser(userRequestDto);

        // Добавляем HATEOAS ссылки
        EntityModel<UserResponseDto> userModel = EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getUserById(createdUser.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserController.class).getUserByEmail(createdUser.getEmail())).withRel("byEmail"));

        return ResponseEntity
                .created(userModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(userModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<EntityModel<UserResponseDto>> getUserById(
            @PathVariable
            @Parameter(description = "ID пользователя")
            Long id) {

        UserResponseDto user = userService.getUserById(id);

        EntityModel<UserResponseDto> userModel = EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserController.class).getUserByEmail(user.getEmail())).withRel("byEmail"));

        return ResponseEntity.ok(userModel);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDto>>> getAllUsers() {

        List<UserResponseDto> users = userService.getAllUsers();

        List<EntityModel<UserResponseDto>> userModels = users.stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getUserByEmail(user.getEmail())).withRel("byEmail")))
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
        Link createLink = linkTo(methodOn(UserController.class).createUser(null)).withRel("create");

        return ResponseEntity.ok(CollectionModel.of(userModels, selfLink, createLink));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Email уже существует")
    })
    public ResponseEntity<EntityModel<UserResponseDto>> updateUser(
            @PathVariable
            @Parameter(description = "ID пользователя")
            Long id,
            @Valid @RequestBody
            @Parameter(description = "Новые данные пользователя")
            UserRequestDto userRequestDto) {

        UserResponseDto updatedUser = userService.updateUser(id, userRequestDto);

        EntityModel<UserResponseDto> userModel = EntityModel.of(updatedUser,
                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserController.class).getUserByEmail(updatedUser.getEmail())).withRel("byEmail"));

        return ResponseEntity.ok(userModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable
            @Parameter(description = "ID пользователя")
            Long id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Получить пользователя по email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<EntityModel<UserResponseDto>> getUserByEmail(
            @PathVariable
            @Parameter(description = "Email пользователя")
            String email) {

        UserResponseDto user = userService.getUserByEmail(email);

        EntityModel<UserResponseDto> userModel = EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserController.class).getUserByEmail(email)).withRel("self"));

        return ResponseEntity.ok(userModel);
    }
}