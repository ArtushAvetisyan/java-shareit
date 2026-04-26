package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> getAllUserItems(Long userId);

    Optional<Item> getItemById(Long itemId);

    List<Item> searchItemByText(String text);

    Item createItem(Item item);

    Item updateItem(Item item);
}