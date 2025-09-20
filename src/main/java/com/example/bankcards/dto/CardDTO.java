package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardDTO {

    // Валидация не нужна, т.к. данные формируются нашим приложением, а не приходят от пользователя (мы контролируем их корректность в сервисном слое)
    Long id;
    String cardNumber; // поле для маскированного номера карты
    String owner;
    LocalDate expirationDate;
    CardStatus status;
    BigDecimal balance;
    Long userId; // Чтобы не разоблачать чувствительные данные(пароль и тд). Также чаще всего нужен только id, а не все данные пользователя

}
