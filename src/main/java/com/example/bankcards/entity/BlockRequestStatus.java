package com.example.bankcards.entity;

public enum BlockRequestStatus {
    PENDING,    // Ожидает рассмотрения администратором
    APPROVED,   // Одобрена администратором, карта заблокирована
    REJECTED    // Отклонена администратором, карта осталась активной
}
