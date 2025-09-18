package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {

    @NotBlank(message = "Введите имя пользователя")
    String username;

    @NotBlank(message = "Введите пароль")
    String password;

}
