package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {

    private final EntityManager entityManager;
    private final ItemService itemService;

    private User owner;
    private Item item;
    private User booker;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = (User.builder().name("Pavel").email("pavel@yandex.ru").build());
        item = Item.builder().name("drill").description("heavy").available(true).owner(owner).build();
        booker = User.builder().name("Booker").email("booker@mail.com").build();

        lastBooking = Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1)).build();

        nextBooking = Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2)).build();

        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.persist(booker);
        entityManager.persist(lastBooking);
        entityManager.persist(nextBooking);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getAllUserItems_shouldReturnAllUserItems() {
        Comment comment = Comment.builder()
                .text("good drill but heavy!").item(item).author(booker)
                .created(LocalDateTime.now()).build();
        entityManager.persist(comment);

        List<ItemResponseDto> result = itemService.getAllUserItems(owner.getId());

        assertThat(result).hasSize(1);
        ItemResponseDto dto = result.getFirst();

        assertThat(dto.getName()).isEqualTo("drill");
        assertThat(dto.getLastBooking().getBookingId()).isEqualTo(lastBooking.getId());
        assertThat(dto.getNextBooking().getBookingId()).isEqualTo(nextBooking.getId());
        assertThat(dto.getComments()).hasSize(1);
    }

    @Test
    void searchItemByText_shouldReturnAllItemsWithText() {
        List<ItemResponseDto> result = itemService.searchItemByText("heavy");

        assertThat(result).hasSize(1);
        ItemResponseDto dto = result.getFirst();

        assertThat(dto.getName()).isEqualTo("drill");
        assertThat(dto.getDescription()).isEqualTo("heavy");
    }
}