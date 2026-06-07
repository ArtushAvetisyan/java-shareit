package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldCreateUser() throws Exception {
        UserDto inputDto = UserDto.builder().id(null).name("Pavel").email("email.yandex.ru").build();
        UserDto outputDto = UserDto.builder().id(1L).name("Pavel").email("email.yandex.ru").build();

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pavel"))
                .andExpect(jsonPath("$.email").value("email.yandex.ru"));

        verify(userService).createUser(inputDto);
    }

    @Test
    void updateUser_shouldUpdateUser() throws Exception {
        long userId = 1L;
        UserDto inputDto = UserDto.builder().id(null).name("Pavel").build();
        UserDto outputDto = UserDto.builder().id(1L).name("Pavel").email("email.yandex.ru").build();

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(outputDto);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pavel"))
                .andExpect(jsonPath("$.email").value("email.yandex.ru"));

        verify(userService).updateUser(userId, inputDto);
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        long userId = 1L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).deleteUser(userId);
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        long userId = 1L;
        UserDto outputDto = UserDto.builder().id(1L).name("Pavel").email("email.yandex.ru").build();

        when(userService.getUserById(anyLong())).thenReturn(outputDto);

        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pavel"))
                .andExpect(jsonPath("$.email").value("email.yandex.ru"));

        verify(userService).getUserById(userId);
    }

    @Test
    void getAllUsers_shouldReturnUsers() throws Exception {
        UserDto outputDto1 = UserDto.builder().id(1L).name("Pavel").email("email.yandex.ru").build();
        UserDto outputDto2 = UserDto.builder().id(2L).name("Ivan").email("email.yandex.ru").build();
        UserDto outputDto3 = UserDto.builder().id(3L).name("Petr").email("email.yandex.ru").build();

        when(userService.getAllUsers()).thenReturn(List.of(outputDto1, outputDto2, outputDto3));

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Pavel"))
                .andExpect(jsonPath("$[0].email").value("email.yandex.ru"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Ivan"))
                .andExpect(jsonPath("$[1].email").value("email.yandex.ru"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Petr"))
                .andExpect(jsonPath("$[2].email").value("email.yandex.ru"));

        verify(userService).getAllUsers();
    }
}