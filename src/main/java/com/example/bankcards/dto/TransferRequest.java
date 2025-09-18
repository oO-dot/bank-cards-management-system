package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {

    @NotNull(message = "Укажите карту отправителя")
    Long fromCardId;

    @NotNull(message = "Укажите карту получателя")
    Long toCardId;

    @NotNull(message = "Укажите сумму перевода")
    @Positive(message = "Сумма перевода должна быть положительной")
    BigDecimal amount;

}
