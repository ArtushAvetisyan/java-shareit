package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.repository.ItemRepositoryImpl;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemServiceImplTest {

    private ItemServiceImpl itemService;
    private UserService userService;
    private ItemRepository itemRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        userService = new UserService(userRepository);
        itemRepository = new ItemRepositoryImpl();
        itemService = new ItemServiceImpl(userService, itemRepository);
    }

    @Test
    void createItem_Success() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        ItemDto itemDto = ItemDto.builder().name("Item").description("Description").available(true).build();

        ItemDto created = itemService.createItem(owner.getId(), itemDto);

        assertNotNull(created.getId());
        assertEquals(itemDto.getName(), created.getName());
        assertEquals(itemDto.getDescription(), created.getDescription());
        assertTrue(created.getAvailable());
    }

    @Test
    void createItem_UserNotFound() {
        ItemDto itemDto = ItemDto.builder().name("Item").description("Description").available(true).build();

        assertThrows(NotFoundException.class, () -> itemService.createItem(99L, itemDto));
    }

    @Test
    void updateItem_Success() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        ItemDto itemDto = itemService.createItem(owner.getId(), ItemDto.builder().name("Item").description("Description").available(true).build());

        ItemDto updateDto = ItemDto.builder().name("Updated Name").description("Updated Description").available(false).build();
        ItemDto updated = itemService.updateItem(owner.getId(), itemDto.getId(), updateDto);

        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void updateItem_PartialUpdate() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        ItemDto itemDto = itemService.createItem(owner.getId(), ItemDto.builder().name("Item").description("Description").available(true).build());

        ItemDto updateDto = ItemDto.builder().name("Only Name Updated").build();
        ItemDto updated = itemService.updateItem(owner.getId(), itemDto.getId(), updateDto);

        assertEquals("Only Name Updated", updated.getName());
        assertEquals("Description", updated.getDescription());
        assertTrue(updated.getAvailable());
    }

    @Test
    void updateItem_WrongOwner() {
        UserDto owner = userService.createUser(UserDto.builder().name("Owner").email("owner@yandex.ru").build());
        UserDto other = userService.createUser(UserDto.builder().name("Other").email("other@yandex.ru").build());
        ItemDto itemDto = itemService.createItem(owner.getId(), ItemDto.builder().name("Item").description("Description").available(true).build());

        ItemDto updateDto = ItemDto.builder().name("Update").build();
        assertThrows(OwnerValidationException.class, () -> itemService.updateItem(other.getId(), itemDto.getId(), updateDto));
    }

    @Test
    void getItemById_Success() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        ItemDto created = itemService.createItem(owner.getId(), ItemDto.builder().name("Item").description("Description").available(true).build());

        ItemDto found = itemService.getItemById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void getItemById_NotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(99L));
    }

    @Test
    void getAllUserItems_Success() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        itemService.createItem(owner.getId(), ItemDto.builder().name("Item 1").description("Desc 1").available(true).build());
        itemService.createItem(owner.getId(), ItemDto.builder().name("Item 2").description("Desc 2").available(true).build());

        List<ItemDto> items = itemService.getAllUserItems(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void searchItemByText_Success() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        itemService.createItem(owner.getId(), ItemDto.builder().name("Drill").description("Power drill").available(true).build());
        itemService.createItem(owner.getId(), ItemDto.builder().name("Screwdriver").description("Tool").available(true).build());

        List<ItemDto> results = itemService.searchItemByText("driLL");

        assertEquals(1, results.size());
        assertEquals("Drill", results.getFirst().getName());
    }

    @Test
    void searchItemByText_EmptyQuery() {
        List<ItemDto> results = itemService.searchItemByText("");
        assertTrue(results.isEmpty());
    }

    @Test
    void searchItemByText_NoMatch() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        itemService.createItem(owner.getId(), ItemDto.builder().name("Drill").description("Power drill").available(true).build());

        List<ItemDto> results = itemService.searchItemByText("banana");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchItemByText_AvailableOnly() {
        UserDto owner = userService.createUser(UserDto.builder().name("User").email("user@yandex.ru").build());
        itemService.createItem(owner.getId(), ItemDto.builder().name("Drill").description("Power drill").available(false).build());

        List<ItemDto> results = itemService.searchItemByText("drill");

        assertTrue(results.isEmpty());
    }
}
