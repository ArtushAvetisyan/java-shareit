package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {

    private final UserService userService;

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        UserDto pavel = UserDto.builder().name("Pavel").email("pavel@yandex.ru").build();
        UserDto katya = UserDto.builder().name("Katya").email("katya@yandex.ru").build();
        UserDto createdPavel = userService.createUser(pavel);
        UserDto createdKatya = userService.createUser(katya);

        List<UserDto> users = userService.getAllUsers();

        assertThat(users).containsExactlyInAnyOrder(createdPavel, createdKatya);
        assertThat(users.size()).isEqualTo(2);
    }
}