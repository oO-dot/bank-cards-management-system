package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/block-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Управление запросами на блокировку", description = "API для администрирования запросов на блокировку карт")
public class AdminBlockRequestController {

    final BlockRequestService blockRequestService;
    final UserService userService;

    // Временное решение до настройки Security
    private Long getCurrentAdminId() {
        // Проверяем права администратора через userService
        if (!userService.isCurrentUserAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
        return userService.getCurrentUserId();
    }

    @GetMapping("/pending")
    @Operation(summary = "Получить ожидающие запросы",
            description = "Возвращает список запросов на блокировку, ожидающих рассмотрения")
    public ResponseEntity<List<BlockRequestDTO>> getPendingRequests() {

        Long adminId = getCurrentAdminId();
        List<BlockRequestDTO> requests = blockRequestService.getPendingRequests(adminId);
        return ResponseEntity.ok(requests);

    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Одобрить запрос на блокировку",
            description = "Одобряет запрос на блокировку карты")
    public ResponseEntity<BlockRequestDTO> approveRequest(@PathVariable Long requestId) {
        Long adminId = getCurrentAdminId();
        BlockRequestDTO approvedRequest = blockRequestService.approveRequest(adminId, requestId);
        return ResponseEntity.ok(approvedRequest);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Отклонить запрос на блокировку",
            description = "Отклоняет запрос на блокировку карты")
    public ResponseEntity<BlockRequestDTO> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {

        Long adminId = getCurrentAdminId();
        BlockRequestDTO rejectedRequest = blockRequestService.rejectRequest(adminId, requestId, reason);
        return ResponseEntity.ok(rejectedRequest);
    }
}
