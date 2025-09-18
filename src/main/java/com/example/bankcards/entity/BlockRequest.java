package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "block_requests")
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    BlockRequestStatus status = BlockRequestStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    User processedBy; // Администратор, обработавший запрос

    @Column(name = "reason", length = 500)
    String reason; // Причина блокировки, указанная пользователем
}
