package com.example.bankcards.entity;

public enum CardStatus {
    ACTIVE, // Карта активна и может использоваться
    BLOCKED, // Карта заблокирована (не может использоваться)
    EXPIRED // Срок действия карты истек
}
