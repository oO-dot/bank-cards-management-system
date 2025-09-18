package com.example.bankcards.util;

import com.example.bankcards.exception.EncryptionException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// класс для шифрования и дешифрования данных с использованием алгоритма AES. Нужен для защиты конфиденциальной информации (Номера банковских карт).
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EncryptionUtil {

    // Секретный ключ для шифрования/дешифрования
    @Value("${encryption.secret-key}")
    String secretKey;

    // final поля должны быть инициализированы сразу или в конструкторе
    // ALGORITHM объявляем как static final, так как это константа
    static final String ALGORITHM = "AES/ECB/PKCS5Padding"; // AES - стандартный и надежный алгоритм шифрования, одобренный для государственных и финансовых систем.

    public String encrypt(String data) {

        try {

            System.out.println("Secret key: " + secretKey); // Добавьте эту строку
            System.out.println("Key length: " + secretKey.getBytes(StandardCharsets.UTF_8).length);

            // Проверка входных данных
            if (data == null || data.trim().isEmpty()) {
                throw new EncryptionException("Данные для шифрования не могут быть пустыми");
            }

            // Создание ключа с явным указанием кодировки
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8); // оборачиваем в массив байт, т.к. SecretKeySpec принимает массив байт
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES"); // оборачиваем массив байт в объект, который понимает AES и представляет собой спецификацию ключа. SecretKeySpec - это класс из Java Cryptography Architecture (JCA),

            // Инициализация шифрования
            Cipher cipher = Cipher.getInstance(ALGORITHM); // Cipher предоставляет методы для работы с криптографией.
            cipher.init(Cipher.ENCRYPT_MODE, keySpec); // Инициализирует Cipher в режиме шифрования (ENCRYPT_MODE) с указанным ключом (keySpec). Теперь cipher готов к работе.

            // Шифрование данных
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)); // cipher.doFinal - выполняет шифрование данных. Возвращает зашифрованные байты.
            return Base64.getEncoder().encodeToString(encryptedBytes); // Кодирует зашифрованные байты в строку Base64. Base64 делает их безопасными для хранения (потому что байты могут содержать непечатаемые символы).

        } catch (Exception e) {
            throw new EncryptionException("Ошибка при шифровании данных", e);
        }

    }

    public String decrypt(String encryptedData) {

        try {

            // Создание ключа
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            // Инициализация дешифрования
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Дешифрование данных
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new EncryptionException("Ошибка при дешифровании данных", e);
        }

    }

}
