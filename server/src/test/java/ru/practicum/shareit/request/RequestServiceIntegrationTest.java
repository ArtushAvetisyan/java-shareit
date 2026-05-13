package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegrationTest {

    private final ItemRequestService requestService;
    private final UserRepository userRepository;

    private User requestAuthor;
    private User anotherRequestAuthor;
    private User user;

    @BeforeEach
    void setUp() {
        requestAuthor = userRepository.save(User.builder().name("Pavel").email("pavel@yandex.ru").build());
        anotherRequestAuthor = userRepository.save(User.builder().name("Vasiliy").email("vasiliy@yandex.ru").build());
        user = userRepository.save(User.builder().name("Ivan").email("ivan@yandex.ru").build());

        for (int i = 0; i < 9; i++) {
            requestService.createRequest(requestAuthor.getId(), ItemRequestDto.builder().description("test " + i).build());
        }
        for (int i = 0; i < 7; i++) {
            requestService.createRequest(anotherRequestAuthor.getId(), ItemRequestDto.builder().description("another test " + i).build());
        }
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() {
        List<ItemRequestResponseDto> requests = requestService.getUserRequests(anotherRequestAuthor.getId());

        assertThat(requests).hasSize(7);
    }

    @Test
    void getUserRequests_shouldReturnSortedByCreatedDesc() {
        List<ItemRequestResponseDto> requests = requestService.getUserRequests(anotherRequestAuthor.getId());

        assertThat(requests).hasSize(7);
        assertThat(requests.getFirst().getCreated()).isAfterOrEqualTo(requests.get(1).getCreated());
    }

    @Test
    void getAllRequests_shouldReturnAllRequests() {
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(user.getId(), 0, 20);

        assertThat(requests).hasSize(16);
    }

    @Test
    void getAllRequests_shouldReturnWithCorrectPagination() {
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(user.getId(), 2, 2);

        assertThat(requests).hasSize(2);
        assertThat(requests.getFirst().getDescription()).isEqualTo("another test 4");
    }

    @Test
    void getAllRequests_shouldReturnSortedByCreatedDesc() {
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(user.getId(), 0, 20);

        assertThat(requests).hasSize(16);
        assertThat(requests.getFirst().getCreated()).isAfterOrEqualTo(requests.get(1).getCreated());
    }
}