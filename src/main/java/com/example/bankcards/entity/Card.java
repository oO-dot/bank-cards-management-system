package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Column(name = "encrypted_number", nullable = false, unique = true, length = 100)
    String encryptedNumber;

    @NotBlank
    @Column(name = "owner", nullable = false, length = 100)
    String owner;

    @NotNull
    @Column(name = "expiration_date", nullable = false)
    LocalDate expirationDate;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    CardStatus status;

    @NotNull
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // подстраховка от этой бесконечной рекурсии(Когда Lombok вызывает user.toString и card.toString и потом в карт опять он вызывает user.toString и так до бесконечности)
    User user;

}
