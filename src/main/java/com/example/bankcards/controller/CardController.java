package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequestDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.service.BlockRequestService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Управление картами", description = "API для работы с банковскими картами")
public class CardController {

    final CardService cardService;
    final BlockRequestService blockRequestService;

    // Временное решение до настройки Security
    private Long getCurrentUserId() {
        return 3L;
    }

    @GetMapping("/me")
    @Operation(summary = "Получить мои карты", description = "Возвращает список карт текущего пользователя")
    public ResponseEntity<Page<CardDTO>> getUserCards(Pageable pageable) {

        Long userId = getCurrentUserId();
        Page<CardDTO> userCards = cardService.getUserCards(userId, pageable);

        return ResponseEntity.ok(userCards);

    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами", description = "Перевод средств между своими картами")
    public ResponseEntity<Void> transferBetweenCards(@Valid @RequestBody TransferRequest transferRequest) {

        cardService.transferBetweenOwnCards(
                transferRequest.getFromCardId(),
                transferRequest.getToCardId(),
                transferRequest.getAmount()
        );

        return  ResponseEntity.ok().build();
    }

    @GetMapping("/{cardId}/balance")
    @Operation(summary = "Получить баланс карты", description = "Возвращает баланс конкретной карты")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long cardId) {

        BigDecimal balance = cardService.getCardBalance(cardId);
        return ResponseEntity.ok(balance);

    }

    @GetMapping
    @Operation(summary = "Получить все карты", description = "Возвращает все карты в системе")
    public ResponseEntity<Page<CardDTO>> getAllCards(Pageable pageable) {

        Page<CardDTO> allCards  = cardService.getAllCards(pageable);
        return ResponseEntity.ok(allCards);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить карту по ID", description = "Возвращает карту по идентификатору")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        CardDTO card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @PostMapping
    @Operation(summary = "Создать карту", description = "Создает новую банковскую карту")
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardRequestDTO cardRequestDTO) {
        CardDTO createdCard = cardService.createCard(cardRequestDTO);
        return ResponseEntity.ok(createdCard);
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Заблокировать карту (админ)", description = "Блокирует карту")
    public ResponseEntity<CardDTO> blockCard(@PathVariable Long id) {
        CardDTO blockedCard = cardService.blockCard(id);
        return ResponseEntity.ok(blockedCard);
    }

    @PostMapping("/{cardId}/block-request")
    @Operation(summary = "Запросить блокировку карты",
            description = "Создает запрос на блокировку карты (только для своих карт)")
    public ResponseEntity<BlockRequestDTO> requestCardBlock(
            @PathVariable Long cardId,
            @RequestParam(required = false) String reason) {

        Long userId = getCurrentUserId();
        BlockRequestDTO request = blockRequestService.createBlockRequest(cardId, userId, reason);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/block-requests/my")
    @Operation(summary = "Получить мои запросы на блокировку",
            description = "Возвращает список запросов на блокировку текущего пользователя")
    public ResponseEntity<List<BlockRequestDTO>> getMyBlockRequests() {
        Long userId = getCurrentUserId();
        List<BlockRequestDTO> requests = blockRequestService.getUserBlockRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Активировать карту", description = "Активирует заблокированную карту")
    public ResponseEntity<CardDTO> activateCard(@PathVariable Long id) {
        CardDTO activatedCard = cardService.activateCard(id);
        return ResponseEntity.ok(activatedCard);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить карту", description = "Удаляет карту")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }

}
