package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.owner.Owner;
import ru.practicum.shareit.request.ItemRequest;

@Data
public class Item {

    private Long id;
    private String name;
    private String description;
    private Boolean isAvailable;
    private Owner owner;
    private ItemRequest request;
}
