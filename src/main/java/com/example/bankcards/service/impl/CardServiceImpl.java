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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
    public Page<CardDTO> getUserCards(Long userId, Pageable pageable) {
        checkUserAccess(userId);
        return cardRepository.findAllByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public void transferBetweenOwnCards(Long userId, Long fromCardId, Long toCardId, BigDecimal amount) {
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Карта отправителя не найдена с id: " + fromCardId));

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new NotFoundException("Карта получателя не найдена с id: " + toCardId));

        if (!fromCard.getUser().getId().equals(userId) ||
                !toCard.getUser().getId().equals(userId)) {
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
    public BigDecimal getCardBalance(Long userId, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Можно просматривать баланс только своих карт");
        }

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

        checkAdminAccess();

        // Валидация номера карты
        validationUtil.validateCardNumber(cardRequestDTO.getNumber());

        User user = userRepository.findById(cardRequestDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с id: " + cardRequestDTO.getUserId()));

        // Проверка срока действия карты
        validationUtil.validateExpirationDate(cardRequestDTO.getExpirationDate());

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
        CardDTO dto = cardMapper.toCardDTO(card);

        // Ручная обработка номера карты: дешифровка + маскирование
        String decryptedNumber = encryptionUtil.decrypt(card.getEncryptedNumber());
        dto.setCardNumber(maskingUtil.maskCardNumber(decryptedNumber));

        return dto;
    }

    private void checkCardAccess(Card card) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new AccessDeniedException("Access denied");
        }

        UserDetails userDetails = (UserDetails) principal;

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String currentUsername = userDetails.getUsername();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!card.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
    }

    /**
     * Проверяет доступ пользователя к данным другого пользователя
     * Администратор имеет доступ ко всем данным
     * Обычный пользователь - только к своим данным
     */
    private void checkUserAccess(Long userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new AccessDeniedException("Access denied");
        }

        UserDetails userDetails = (UserDetails) principal;

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String currentUsername = userDetails.getUsername();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
    }

    /**
     * Проверяет, является ли пользователь администратором
     */
    private void checkAdminAccess() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new AccessDeniedException("Access denied");
        }

        UserDetails userDetails = (UserDetails) principal;

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
