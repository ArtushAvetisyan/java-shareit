package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegrationTest {

    private final ItemRequestService requestService;
    private final EntityManager entityManager;

    private User requestAuthor;
    private User anotherRequestAuthor;
    private User user;

    @BeforeEach
    void setUp() {
        requestAuthor = User.builder().name("Pavel").email("pavel@yandex.ru").build();
        anotherRequestAuthor = User.builder().name("Vasiliy").email("vasiliy@yandex.ru").build();
        user = User.builder().name("Ivan").email("ivan@yandex.ru").build();

        entityManager.persist(requestAuthor);
        entityManager.persist(anotherRequestAuthor);
        entityManager.persist(user);

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 9; i++) {
            ItemRequest request = ItemRequest.builder()
                    .description("Request description " + i)
                    .requester(requestAuthor)
                    .created(now.plusHours(i).withNano(0)).build();
            entityManager.persist(request);
        }

        for (int i = 0; i < 7; i++) {
            ItemRequest request = ItemRequest.builder()
                    .description("Request description " + i)
                    .requester(anotherRequestAuthor)
                    .created(now.plusHours(i).withNano(0)).build();
            entityManager.persist(request);
        }
        entityManager.flush();
        entityManager.clear();
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
        assertThat(requests.getFirst().getDescription()).isEqualTo("Request description 6");
    }

    @Test
    void getAllRequests_shouldReturnSortedByCreatedDesc() {
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(user.getId(), 0, 20);

        assertThat(requests).hasSize(16);
        assertThat(requests.getFirst().getCreated()).isAfterOrEqualTo(requests.get(1).getCreated());
    }
}