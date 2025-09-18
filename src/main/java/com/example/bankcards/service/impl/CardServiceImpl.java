package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.CardRequestMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.MaskingUtil;
import com.example.bankcards.util.ValidationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardServiceImpl implements CardService {

    final CardRepository cardRepository;
    final UserRepository userRepository;
    final CardMapper cardMapper;
    final CardRequestMapper cardRequestMapper;
    final EncryptionUtil encryptionUtil;
    final MaskingUtil maskingUtil;
    final ValidationUtil validationUtil;

    @Override
    public List<CardDTO> getUserCards(Long userId) {
        checkUserAccess(userId);
        return cardRepository.findAllByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Page<CardDTO> getUserCards(Long userId, Pageable pageable) {
        checkUserAccess(userId);
        return cardRepository.findAllByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public void transferBetweenOwnCards(Long fromCardId, Long toCardId, BigDecimal amount) {

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Карта отправителя не найдена с id: " + fromCardId));

        User currentUser = fromCard.getUser();

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new NotFoundException("Карта получателя не найдена с id: " + toCardId));

        if (!fromCard.getUser().getId().equals(toCard.getUser().getId()) ||
                !toCard.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете переводить только между своими картами");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new ValidationException("Обе карты должны быть активны для перевода");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Недостаточно средств на карте отправителя");
        }

        validationUtil.validateAmount(amount);

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

    }

    @Override
    public BigDecimal getCardBalance(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        checkCardAccess(card);
        return card.getBalance();
    }

    @Override
    public CardDTO getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена с id: " + id));

        checkCardAccess(card);
        return convertToDto(card);
    }

    @Override
    public Page<CardDTO> getAllCards(Pageable pageable) {

        checkAdminAccess();

        return cardRepository.findAll(pageable)
                .map(this::convertToDto);

    }

    @Override
    @Transactional
    public CardDTO createCard(CardRequestDTO cardRequestDTO) {

        //checkAdminAccess();

        // Валидация номера карты
        validationUtil.validateCardNumber(cardRequestDTO.getNumber());

        // Проверка существования пользователя
        User user = userRepository.findById(cardRequestDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с id: " + cardRequestDTO.getUserId()));

        // Проверка срока действия карты
        validationUtil.validateExpirationDate(cardRequestDTO.getExpirationDate());

        // Создание карты
        Card card = cardRequestMapper.toEntity(cardRequestDTO);

        card.setEncryptedNumber(encryptionUtil.encrypt(cardRequestDTO.getNumber()));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }

    @Override
    @Transactional
    public CardDTO blockCard(Long cardId) {
        checkAdminAccess();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена с id: " + cardId));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ValidationException("Карта уже заблокирована");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new ValidationException("Нельзя заблокировать просроченную карту");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        return convertToDto(updatedCard);
    }

    @Override
    @Transactional
    public CardDTO activateCard(Long cardId) {
        checkAdminAccess();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new ValidationException("Можно активировать только заблокированные карты");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);
        return convertToDto(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        checkAdminAccess();

        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Карта не найдена с id: " + cardId);
        }

        cardRepository.deleteById(cardId);
    }

    /**
     * Комбинированный подход: MapStruct для простых полей + ручная обработка для сложных
     */
    private CardDTO convertToDto(Card card) {
        // Автоматический маппинг простых полей
        CardDTO dto = cardMapper.toCardDTO(card);

        // Ручная обработка номера карты: дешифровка + маскирование
        String decryptedNumber = encryptionUtil.decrypt(card.getEncryptedNumber());
        dto.setCardNumber(maskingUtil.maskCardNumber(decryptedNumber));

        return dto;
    }

    private void checkUserAccess(Long userId) {
        /*String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
         */
        return;
    }

    private void checkCardAccess(Card card) {
       /* String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getRole().name().equals("ADMIN") && !card.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        */

        return;
    }

    private void checkAdminAccess() {
        /*String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new AccessDeniedException("Admin access required");
        }

         */

        return;
    }
}
