package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateDTO {
    @NotBlank(message = "Введите имя")
    String firstName;

    @NotBlank(message = "Введите фамилию")
    String lastName;

    @NotNull(message = "Укажите роль")
    Role role;

    Boolean enabled; // Добавляем поле для блокировки/активации

}
