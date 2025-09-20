package com.example.bankcards.service;

import com.example.bankcards.dto.BlockRequestDTO;

import java.util.List;

public interface BlockRequestService {
    BlockRequestDTO createBlockRequest(Long userId, Long cardId, String reason);

    List<BlockRequestDTO> getUserBlockRequests(Long userId);

    List<BlockRequestDTO> getPendingRequests(Long adminId);

    BlockRequestDTO approveRequest(Long adminId, Long requestId);

    BlockRequestDTO rejectRequest(Long adminId, Long requestId, String rejectionReason);
}
