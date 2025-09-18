package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardRequestDTO {

    @NotBlank(message = "Введите номер карты")
    String number;

    @NotBlank(message = "Введите имя владельца")
    String owner;

    @NotNull(message = "Укажите дату окончания действия")
    LocalDate expirationDate;

    @NotNull(message = "Укажите пользователя")
    Long userId; // Можно проверить, существует ли пользователь с таким ID. Отделяем данные запроса от бизнес-логики. Для создания новой карты нужно указать, какому пользователю она принадлежит.

}
