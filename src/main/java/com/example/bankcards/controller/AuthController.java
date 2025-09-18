package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Аутентификация", description = "API для входа в систему")
public class AuthController {

    final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токена")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }

}
