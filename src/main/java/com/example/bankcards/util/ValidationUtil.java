package com.example.bankcards.util;

import com.example.bankcards.exception.ValidationException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

//  Класс, который содержит методы для проверки (валидации) различных данных, связанных с банковскими картами.
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidationUtil {

    // Константы для валидации номеров карт
    static final int MIN_CARD_LENGTH = 13;
    static final int MAX_CARD_LENGTH = 19;

    // Делаем лимиты настраиваемыми через конфигурацию
    @Value("${validation.max-transfer-amount:100000000}") // 100 млн по умолчанию
    BigDecimal maxTransferAmount;

    @Value("${validation.max-balance:1000000000}") // 1 млрд по умолчанию
    BigDecimal maxBalance;

    // Алгоритм Луна для проверки номеров карт
    public boolean isValidCardNumber(String cardNumber) {

        if (!StringUtils.hasText(cardNumber)) {
            return false;
        }

        // Удаляем все нецифровые символы
        String digitsOnly = cardNumber.replaceAll("\\D", "");

        // Проверяем длину номера карты
        if (digitsOnly.length() < MIN_CARD_LENGTH || digitsOnly.length() > MAX_CARD_LENGTH) {
            return false;
        }

        // Реализация алгоритма Луна
        int sum = 0;
        boolean alternate = false;

        for (int i = digitsOnly.length() - 1; i >= 0 ; i--) {
            int digit = Character.getNumericValue(digitsOnly.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    public void validateCardNumber(String cardNumber) {
        if (!isValidCardNumber(cardNumber)) {
            throw new ValidationException("Неверный номер карты");
        }
    }


    public boolean isValidAmount(BigDecimal amount) {

        return amount != null &&
                amount.compareTo(BigDecimal.ZERO) > 0 &&
                amount.compareTo(maxTransferAmount) <= 0;

    }

    public void validateAmount(BigDecimal amount) {
        if (!isValidAmount(amount)) {
            throw new ValidationException(
                    String.format("Неверная сумма. Должна быть положительной и не превышать %s", maxTransferAmount)
            );
        }
    }

    public boolean isValidExpirationDate(LocalDate expirationDate) {

        return expirationDate != null && !expirationDate.isBefore(LocalDate.now());

    }

    public void validateExpirationDate(LocalDate expirationDate) {

        if (expirationDate == null) {
            throw new ValidationException("Дата окончания действия не может быть null");
        }

        if (expirationDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Срок действия карты истек");
        }

    }

    public boolean isValidBalance(BigDecimal balance) {

        return balance != null &&
                balance.compareTo(BigDecimal.ZERO) >= 0 &&
                balance.compareTo(maxBalance) <= 0;

    }

    public void validateBalance(BigDecimal balance) {
        if (!isValidBalance(balance)) {
            throw new ValidationException(
                    String.format("Неверный баланс. Должен быть неотрицательным и не превышать %s", maxBalance)
            );
        }
    }

}

