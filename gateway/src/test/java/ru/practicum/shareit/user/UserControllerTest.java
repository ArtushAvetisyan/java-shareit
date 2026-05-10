package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createUser_whenNameIsEmpty_shouldReturnBadRequest() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("")
                .email("name@yandex.ru")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(invalidUser))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }

    @Test
    void createUser_whenEmailIsInvalid_shouldReturnBadRequest() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("User")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(invalidUser))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }

    @Test
    void createUser_whenDataIsValid_shouldCreateUser() throws Exception {
        UserDto validUser = UserDto.builder()
                .name("User")
                .email("name@yandex.ru")
                .build();

        when(userClient.createUser(any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(validUser))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).createUser(any());
    }

    @Test
    void updateUser_whenNameAndEmailAreInvalid_shouldReturnBadRequest() throws Exception {
        long userId = 1L;
        UserDto invalidUser = UserDto.builder()
                .name("")
                .email("invalid-email")
                .build();

        mockMvc.perform(patch("/users/{id}", userId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(invalidUser))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).updateUser(anyLong(), any());
    }

    @Test
    void updateUser_whenNameIsValid_shouldUpdateUser() throws Exception {
        long userId = 1L;
        UserDto validUser = UserDto.builder()
                .name("Pavel")
                .build();

        when(userClient.updateUser(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{id}", userId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validUser))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).updateUser(anyLong(), any());
    }

    @Test
    void updateUser_whenEmailIsValid_shouldUpdateUser() throws Exception {
        long userId = 1L;
        UserDto validUser = UserDto.builder()
                .email("pavel@yandex.ru")
                .build();

        when(userClient.updateUser(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{id}", userId)
                        .header(USER_ID_HEADER, userId)
                        .content(objectMapper.writeValueAsString(validUser))
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).updateUser(anyLong(), any());
    }

    @Test
    void deleteUser_whenDeleteRequest_shouldDeleteUser() throws Exception {
        long userId = 1L;

        when(userClient.deleteUser(userId)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).deleteUser(eq(userId));
    }

    @Test
    void getUserById_whenGetRequest_shouldReturnUser() throws Exception {
        long userId = 1L;
        UserDto expectedUser = UserDto.builder().id(userId).name("Vasiliy").email("user@yandex.ru").build();

        when(userClient.getUser(userId)).thenReturn(ResponseEntity.ok(expectedUser));

        mockMvc.perform(get("/users/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Vasiliy"))
                .andExpect(jsonPath("$.email").value("user@yandex.ru"));

        verify(userClient).getUser(eq(userId));
    }

    @Test
    void getAllUsers_whenGetRequest_shouldReturnUsers() throws Exception {
        when(userClient.getUsers()).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).getUsers();
    }
}