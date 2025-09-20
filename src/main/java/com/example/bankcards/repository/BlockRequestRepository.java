package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {

    @EntityGraph(attributePaths = {"user", "card", "processedBy"})
    List<BlockRequest> findByStatus(BlockRequestStatus status);

    @EntityGraph(attributePaths = {"user", "card", "processedBy"})
    List<BlockRequest> findByUserId(Long userId);

    boolean existsByCardIdAndStatus(Long cardId, BlockRequestStatus status);

    Optional<BlockRequest> findByCardIdAndStatus(Long cardId, BlockRequestStatus status);
}