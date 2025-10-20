package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardRequestDTO;
import com.example.bankcards.entity.Card;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-20T03:34:39+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class CardRequestMapperImpl implements CardRequestMapper {

    @Override
    public Card toEntity(CardRequestDTO cardRequestDTO) {
        if ( cardRequestDTO == null ) {
            return null;
        }

        Card card = new Card();

        card.setOwner( cardRequestDTO.getOwner() );
        card.setExpirationDate( cardRequestDTO.getExpirationDate() );

        return card;
    }
}
