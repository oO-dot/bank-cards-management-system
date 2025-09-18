package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CardService {
    // Для пользователя
    List<CardDTO> getUserCards(Long userId);
    Page<CardDTO> getUserCards(Long userId, Pageable pageable); // С пагинацией
    void transferBetweenOwnCards(Long fromCardId, Long toCardId, BigDecimal amount);
    BigDecimal getCardBalance(Long cardId);

    // Для администратора
    CardDTO getCardById(Long id);
    Page<CardDTO> getAllCards(Pageable pageable); // Все карты с пагинацией
    CardDTO createCard(CardRequestDTO cardRequestDTO);
    CardDTO blockCard(Long cardId);
    CardDTO activateCard(Long cardId);
    void deleteCard(Long cardId);
}
