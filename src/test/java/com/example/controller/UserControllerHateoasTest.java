package com.example.controller;

import com.example.dto.UserResponseDto;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerHateoasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUserById_ShouldReturnHateoasLinks() throws Exception {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(1L)).thenReturn(user);

        MvcResult result = mockMvc.perform(get("/api/users/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.users.href").exists())
                .andExpect(jsonPath("$._links.byEmail.href").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(content);

        assertThat(root.has("_links")).isTrue();
        assertThat(root.get("_links").has("self")).isTrue();
        assertThat(root.get("_links").get("self").get("href").asText())
                .contains("/api/users/1");
    }

    @Test
    void getAllUsers_ShouldReturnCollectionWithLinks() throws Exception {
        mockMvc.perform(get("/api/users")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.create.href").exists());
    }
}