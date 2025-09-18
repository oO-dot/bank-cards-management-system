package com.example.bankcards.dto;

import com.example.bankcards.entity.BlockRequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockRequestDTO {
    Long id;
    Long cardId;
    String maskedCardNumber; // Для отображения пользователю
    Long userId;
    String userFullName; // Для отображения администратору
    BlockRequestStatus status;
    LocalDateTime createdAt;
    LocalDateTime processedAt;
    Long processedBy;
    String reason;
}
