package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Pavel", "pavel@yandex.ru");
        userDto = UserMapper.toUserDto(user);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto, result.getFirst());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUserDtoWhenFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrowNotFoundExceptionWhenNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_shouldReturnUserDtoWhenSuccessful() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(userRepository).save(UserMapper.toUser(userDto));
    }

    @Test
    void createUser_shouldThrowConflictExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.createUser(userDto));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldReturnUpdatedUserDtoWhenSuccessful() {
        long userId = 1L;
        UserDto userRequestDto = UserDto.builder().name("Vasiliy").email("vasiliy@yandex.ru").build();
        User updatedUser = new User(userId, "Vasiliy", "vasiliy@yandex.ru");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserDto result = userService.updateUser(userId, userRequestDto);

        assertNotNull(result);
        assertEquals("Vasiliy", result.getName());
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(userRequestDto.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowNotFoundExceptionWhenUserToUpdateNotFound() {
        UserDto updatedUserDto = UserDto.builder().id(1L).name("Vasiliy").email("vasiliy@yandex.ru").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.updateUser(1L, updatedUserDto));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowConflictExceptionWhenNewEmailExists() {
        UserDto updatedUserDto = UserDto.builder().id(1L).name("Vasiliy").email("vasiliy@yandex.ru").build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.updateUser(1L, updatedUserDto));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail(updatedUserDto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldUpdateOnlyNameWhenEmailIsNull() {
        UserDto secondUserDto = UserDto.builder().name("Vasiliy").email(null).build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, secondUserDto);

        assertNotNull(result);
        assertEquals("Vasiliy", result.getName());
        assertEquals("pavel@yandex.ru", result.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldUpdateOnlyEmailWhenNameIsNull() {
        UserDto secondUserDto = UserDto.builder().id(null).name(null).email("vasiliy@yandex.ru").build();
        User expectedUser = new User(1L, "Pavel", "vasiliy@yandex.ru");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, secondUserDto);

        assertNotNull(result);
        assertEquals("Pavel", result.getName());
        assertEquals("vasiliy@yandex.ru", result.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail(secondUserDto.getEmail());
        verify(userRepository).save(expectedUser);
    }

    @Test
    void updateUser_shouldNotUpdateWhenNameAndEmailAreNull() {
        UserDto secondUserDto = UserDto.builder().id(null).name(null).email(null).build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(1L, secondUserDto);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_shouldDeleteUserWhenFound() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowNotFoundExceptionWhenNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}