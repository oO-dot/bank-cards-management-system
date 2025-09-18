package com.example.bankcards.util;

import com.example.bankcards.exception.InvalidCardDataException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaskingUtil {

    // Константы для шаблонов маскирования
    static final String CARD_MASK_PATTERN = "**** **** **** ";
    static final int MIN_CARD_LENGTH = 13;
    static final int MAX_CARD_LENGTH = 19;

    public String maskCardNumber(String cardNumber) {

        if (!StringUtils.hasText(cardNumber)) {
            throw new InvalidCardDataException("Номер карты не может быть null или пустым");
        }

        // Удаляем все нецифровые символы (пробелы, дефисы и т.д.)
        String digitsOnly = cardNumber.replaceAll("\\D", "");

        // Проверяем длину номера карты согласно стандартам
        if (digitsOnly.length() < MIN_CARD_LENGTH || digitsOnly.length() > MAX_CARD_LENGTH) {
            throw new InvalidCardDataException(
                    String.format("Номер карты должен содержать от %d до %d цифр. Получено: %d цифр",
                            MIN_CARD_LENGTH, MAX_CARD_LENGTH, digitsOnly.length())
            );
        }

        // Получаем последние 4 цифры номера карты
        String lastFour = digitsOnly.substring(digitsOnly.length() - 4);

        // Собираем маскированный номер: "**** **** **** " + последние 4 цифры
        return CARD_MASK_PATTERN + lastFour;

    }
}
