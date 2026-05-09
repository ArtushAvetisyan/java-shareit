package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.validation.Create;
import ru.practicum.shareit.user.dto.validation.Update;

@Data
@Builder
public class UserDto {

    private Long id;

    @NotBlank(groups = Create.class, message = "Имя пользователя не может быть пустым")
    private String name;

    @Email(groups = {Create.class, Update.class}, message = "Некорректный адрес электронной почты")
    @NotBlank(groups = Create.class, message = "Адрес электронной почты не может быть пустым")
    private String email;
}