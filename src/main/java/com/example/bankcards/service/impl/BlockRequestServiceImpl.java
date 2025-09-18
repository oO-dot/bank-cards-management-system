package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.mapper.BlockRequestMapper;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.MaskingUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockRequestServiceImpl implements BlockRequestService {

    final BlockRequestRepository blockRequestRepository;
    final CardRepository cardRepository;
    final UserRepository userRepository;
    final BlockRequestMapper blockRequestMapper;
    final MaskingUtil maskingUtil;
    final EncryptionUtil encryptionUtil;

    @Override
    @Transactional
    public BlockRequestDTO createBlockRequest(Long cardId, Long userId, String reason) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!card.getUser().getId().equals(userId)) {
            throw new ValidationException("Карта не принадлежит пользователю");
        }

        if (blockRequestRepository.existsByCardIdAndStatus(cardId, BlockRequestStatus.PENDING)) {
            throw new ValidationException("Уже есть активный запрос на блокировку этой карты");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ValidationException("Можно запросить блокировку только активной карты");
        }

        BlockRequest blockRequest = new BlockRequest();
        blockRequest.setCard(card);
        blockRequest.setUser(user);
        blockRequest.setReason(reason);
        blockRequest.setStatus(BlockRequestStatus.PENDING);
        blockRequest.setCreatedAt(LocalDateTime.now());

        BlockRequest savedRequest = blockRequestRepository.save(blockRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public List<BlockRequestDTO> getUserBlockRequests(Long userId) {
        List<BlockRequest> requests = blockRequestRepository.findByUserId(userId);
        return requests.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<BlockRequestDTO> getPendingRequests() {
        List<BlockRequest> requests = blockRequestRepository.findByStatus(BlockRequestStatus.PENDING);
        return requests.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public BlockRequestDTO approveRequest(Long requestId, Long adminId) {
        BlockRequest blockRequest = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Администратор не найден"));

        if (admin.getRole() != Role.ADMIN) {
            throw new ValidationException("Только администратор может одобрять запросы");
        }

        if (blockRequest.getStatus() != BlockRequestStatus.PENDING) {
            throw new ValidationException("Запрос уже обработан");
        }

        Card card = blockRequest.getCard();
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        blockRequest.setStatus(BlockRequestStatus.APPROVED);
        blockRequest.setProcessedAt(LocalDateTime.now());
        blockRequest.setProcessedBy(admin);

        BlockRequest updatedRequest = blockRequestRepository.save(blockRequest);
        return convertToDto(updatedRequest);
    }

    @Override
    @Transactional
    public BlockRequestDTO rejectRequest(Long requestId, Long adminId, String rejectionReason) {
        BlockRequest blockRequest = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Администратор не найден"));

        if (admin.getRole() != Role.ADMIN) {
            throw new ValidationException("Только администратор может отклонять запросы");
        }

        if (blockRequest.getStatus() != BlockRequestStatus.PENDING) {
            throw new ValidationException("Запрос уже обработан");
        }

        blockRequest.setStatus(BlockRequestStatus.REJECTED);
        blockRequest.setProcessedAt(LocalDateTime.now());
        blockRequest.setProcessedBy(admin);
        if (rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            blockRequest.setReason(blockRequest.getReason() + " (Причина отклонения: " + rejectionReason + ")");
        }

        BlockRequest updatedRequest = blockRequestRepository.save(blockRequest);
        return convertToDto(updatedRequest);
    }

    private BlockRequestDTO convertToDto(BlockRequest blockRequest) {
        BlockRequestDTO dto = blockRequestMapper.toDto(blockRequest);

        // ДОБАВЛЕНО: Обработка маскировки номера карты
        try {
            String decryptedNumber = encryptionUtil.decrypt(blockRequest.getCard().getEncryptedNumber());
            dto.setMaskedCardNumber(maskingUtil.maskCardNumber(decryptedNumber));
        } catch (Exception e) {
            dto.setMaskedCardNumber("**** **** **** ****");
        }

        return dto;
    }
}
