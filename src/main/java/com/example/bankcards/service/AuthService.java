package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(String username, String password);
}
