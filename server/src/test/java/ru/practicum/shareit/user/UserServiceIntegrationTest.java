package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {

    private final UserService userService;
    private final EntityManager entityManager;

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        User pavel = User.builder().name("Pavel").email("pavel@yandex.ru").build();
        User katya = User.builder().name("Katya").email("katya@yandex.ru").build();
        entityManager.persist(pavel);
        entityManager.persist(katya);
        entityManager.flush();
        entityManager.clear();

        List<UserDto> users = userService.getAllUsers();

        assertThat(users.size()).isEqualTo(2);
        assertThat(users)
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("pavel@yandex.ru", "katya@yandex.ru");
    }
}