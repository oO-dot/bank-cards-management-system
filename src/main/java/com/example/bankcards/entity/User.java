package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Column(name = "username", unique = true, nullable = false, length = 50)
    String username; // Логин пользователя (уникальный)

    @NotBlank
    @Column(name = "password", nullable = false, length = 100)
    String password; // Зашифрованный пароль

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 50)
    String firstName; // Имя пользователя

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 50)
    String lastName; // Фамилия пользователя

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    Role role; // Роль (USER или ADMIN) - определяет права доступа

    @Column(name = "enabled", nullable = false)
    Boolean enabled = true; // Активен ли пользователь (можно заблокировать)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // подстраховка от этой бесконечной рекурсии(Когда Lombok вызывает user.toString и card.toString и потом в кард опять он вызывает user.toString и так до бесконечности)
    List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
        card.setUser(this);
    }

    public void removeCard(Card card) {
        cards.remove(card);
        card.setUser(null);
    }


}
