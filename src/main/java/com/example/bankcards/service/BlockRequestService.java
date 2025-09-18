package com.example.bankcards.service;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.entity.BlockRequestStatus;

import java.util.List;

public interface BlockRequestService {
    BlockRequestDTO createBlockRequest(Long cardId, Long userId, String reason);
    List<BlockRequestDTO> getUserBlockRequests(Long userId);
    List<BlockRequestDTO> getPendingRequests();
    BlockRequestDTO approveRequest(Long requestId, Long adminId);
    BlockRequestDTO rejectRequest(Long requestId, Long adminId, String rejectionReason);
}
