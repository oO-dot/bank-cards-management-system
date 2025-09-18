package com.example.bankcards.mapper;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-18T08:17:15+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class BlockRequestMapperImpl implements BlockRequestMapper {

    @Override
    public BlockRequestDTO toDto(BlockRequest blockRequest) {
        if ( blockRequest == null ) {
            return null;
        }

        BlockRequestDTO.BlockRequestDTOBuilder blockRequestDTO = BlockRequestDTO.builder();

        blockRequestDTO.cardId( blockRequestCardId( blockRequest ) );
        blockRequestDTO.userId( blockRequestUserId( blockRequest ) );
        blockRequestDTO.userFullName( getUserFullName( blockRequest.getUser() ) );
        blockRequestDTO.processedBy( blockRequestProcessedById( blockRequest ) );
        blockRequestDTO.id( blockRequest.getId() );
        blockRequestDTO.status( blockRequest.getStatus() );
        blockRequestDTO.createdAt( blockRequest.getCreatedAt() );
        blockRequestDTO.processedAt( blockRequest.getProcessedAt() );
        blockRequestDTO.reason( blockRequest.getReason() );

        return blockRequestDTO.build();
    }

    private Long blockRequestCardId(BlockRequest blockRequest) {
        Card card = blockRequest.getCard();
        if ( card == null ) {
            return null;
        }
        return card.getId();
    }

    private Long blockRequestUserId(BlockRequest blockRequest) {
        User user = blockRequest.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private Long blockRequestProcessedById(BlockRequest blockRequest) {
        User processedBy = blockRequest.getProcessedBy();
        if ( processedBy == null ) {
            return null;
        }
        return processedBy.getId();
    }
}
