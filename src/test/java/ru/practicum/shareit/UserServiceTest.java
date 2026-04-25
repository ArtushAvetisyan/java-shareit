package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest{

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        userService = new UserService(userRepository);
    }

    @Test
    void getAllUsers_WhenEmpty_ReturnsEmptyList() {
        List<UserDto> users = userService.getAllUsers();
        assertTrue(users.isEmpty(), "Список пользователей должен быть пустым");
    }

    @Test
    void getAllUsers_WhenNotEmpty_ReturnsList() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());
        userService.createUser(UserDto.builder().name("User 2").email("user2@yandex.ru").build());

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size(), "Размер списка должен быть 2");
        assertEquals("User 1", users.get(0).getName());
        assertEquals("User 2", users.get(1).getName());
    }

    @Test
    void getUserById_WhenExists_ReturnsUser() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        UserDto userDto = userService.getUserById(1L);

        assertNotNull(userDto);
        assertEquals("User 1", userDto.getName());
        assertEquals("user1@yandex.ru", userDto.getEmail());
    }

    @Test
    void getUserById_WhenNotExists_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(1L),
                "Должно быть выброшено исключение NotFoundException");
    }

    @Test
    void createUser_WhenEmailUnique_ReturnsCreatedUser() {
        UserDto userDto = UserDto.builder().name("User 1").email("user1@yandex.ru").build();

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void createUser_WhenEmailExists_ThrowsValidationException() {
        UserDto user1 = UserDto.builder().name("User 1").email("user@yandex.ru").build();
        userService.createUser(user1);

        UserDto user2 = UserDto.builder().name("User 2").email("user@yandex.ru").build();

        assertThrows(ValidationException.class, () -> userService.createUser(user2),
                "Должно быть выброшено исключение ValidationException при дубликате email");
    }

    @Test
    void updateUser_WhenUserExistsAndValidData_UpdatesUser() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        UserDto updateDto = UserDto.builder().name("Updated").email("updated@yandex.ru").build();
        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("Updated", result.getName());
        assertEquals("updated@yandex.ru", result.getEmail());
    }

    @Test
    void updateUser_WhenOnlyNameProvided_UpdatesOnlyName() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        UserDto updateDto = UserDto.builder().name("Updated").build();
        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("Updated", result.getName());
        assertEquals("user1@yandex.ru", result.getEmail());
    }

    @Test
    void updateUser_WhenOnlyEmailProvided_UpdatesOnlyEmail() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        UserDto updateDto = UserDto.builder().email("updated@yandex.ru").build();
        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("User 1", result.getName());
        assertEquals("updated@yandex.ru", result.getEmail());
    }

    @Test
    void updateUser_WhenEmailIsSame_DoesNotThrowConflict() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        UserDto updateDto = UserDto.builder().email("user1@yandex.ru").build();
        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("user1@yandex.ru", result.getEmail());
    }

    @Test
    void updateUser_WhenEmailTakenByOther_ThrowsConflictException() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());
        userService.createUser(UserDto.builder().name("User 2").email("user2@yandex.ru").build());

        UserDto updateDto = UserDto.builder().email("user2@yandex.ru").build();

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, updateDto),
                "Должно быть выброшено исключение ConflictException если email занят другим пользователем");
    }

    @Test
    void updateUser_WhenUserNotFound_ThrowsNotFoundException() {
        UserDto updateDto = UserDto.builder().name("Updated").build();
        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, updateDto));
    }

    @Test
    void updateUser_WhenFieldsBlankOrNull_DoesNotUpdate() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        UserDto updateDto = UserDto.builder().name("").email(null).build();
        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("User 1", result.getName(), "Имя не должно было измениться при пустой строке");
        assertEquals("user1@yandex.ru", result.getEmail(), "Email не должен был измениться при null");
    }

    @Test
    void deleteUser_WhenUserExists_DeletesUser() {
        userService.createUser(UserDto.builder().name("User 1").email("user1@yandex.ru").build());

        userService.deleteUser(1L);

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L),
                "Пользователь должен быть удален");
    }

    @Test
    void deleteUser_WhenUserNotFound_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }
}
