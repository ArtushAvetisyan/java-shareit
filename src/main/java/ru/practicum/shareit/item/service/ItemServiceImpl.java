package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;

    public List<ItemDto> getAllUserItems(Long userId) {
        userService.getUserById(userId);
        return itemRepository.getAllUserItems(userId).stream().map(ItemMapper::toItemDto).toList();
    }

    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.getItemById(itemId).orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> searchItemByText(String text) {
        if (text.isBlank()) return Collections.emptyList();
        return itemRepository.searchItemByText(text).stream().map(ItemMapper::toItemDto).toList();
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        UserDto userDto = userService.getUserById(userId);
        User owner = UserMapper.toUser(userDto);
        owner.setId(userDto.getId());

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.saveItem(item));
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = itemRepository.getItemById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id " + itemId + " не найден"));
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new OwnerValidationException("Вещь не пренадлежит пользователю. Редактирование запрещено");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.saveItem(item));
    }
}