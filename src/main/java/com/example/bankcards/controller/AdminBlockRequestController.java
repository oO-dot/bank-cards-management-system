package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.service.BlockRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/block-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Управление запросами на блокировку", description = "API для администрирования запросов на блокировку карт")
public class AdminBlockRequestController {

    final BlockRequestService blockRequestService;

    // Временное решение до настройки Security
    private Long getCurrentAdminId() {
        return 1L; // ID администратора из ваших тестовых данных
    }

    @GetMapping("/pending")
    @Operation(summary = "Получить ожидающие запросы",
            description = "Возвращает список запросов на блокировку, ожидающих рассмотрения")
    public ResponseEntity<List<BlockRequestDTO>> getPendingRequests() {
        List<BlockRequestDTO> requests = blockRequestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Одобрить запрос на блокировку",
            description = "Одобряет запрос на блокировку карты")
    public ResponseEntity<BlockRequestDTO> approveRequest(@PathVariable Long requestId) {
        Long adminId = getCurrentAdminId();
        BlockRequestDTO approvedRequest = blockRequestService.approveRequest(requestId, adminId);
        return ResponseEntity.ok(approvedRequest);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Отклонить запрос на блокировку",
            description = "Отклоняет запрос на блокировку карты")
    public ResponseEntity<BlockRequestDTO> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {

        Long adminId = getCurrentAdminId();
        BlockRequestDTO rejectedRequest = blockRequestService.rejectRequest(requestId, adminId, reason);
        return ResponseEntity.ok(rejectedRequest);
    }
}
