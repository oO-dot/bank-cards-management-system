package com.example.bankcards.service.impl;

import com.example.bankcards.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public String login(String username, String password) {
        // Реализация будет добавлена при настройке Spring Security
        // Пока просто заглушка
        return "jwt-token-stub"; //TODO
    }
}
