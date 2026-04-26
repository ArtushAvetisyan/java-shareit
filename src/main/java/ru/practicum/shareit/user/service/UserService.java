package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream().map(UserMapper::toUserDto).toList();
    }

    public UserDto getUserById(Long id) {
        Optional<User> user = userRepository.getUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return UserMapper.toUserDto(user.get());
    }

    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        User createdUser = userRepository.createUser(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(createdUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        Optional<User> user = userRepository.getUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        User checkedUser = user.get();
        if (userDto.getEmail() != null && !userDto.getEmail().equals(checkedUser.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже существует");
            }
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            checkedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            checkedUser.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.updateUser(id, checkedUser));
    }

    public void deleteUser(Long id) {
        if (!userRepository.deleteUser(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}