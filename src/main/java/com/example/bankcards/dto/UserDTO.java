package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {

    Long id;

    @NotBlank(message = "Введите имя пользователя")
    String username;

    @NotBlank(message = "Введите имя")
    String firstName;

    @NotBlank(message = "Введите фамилию")
    String lastName;

    @NotBlank(message = "Укажите роль")
    String role; // сокрытие реализации(Клиенту не нужно знать о внутренней enum-структуре). Если изменится логика API останется стабильным.

}
