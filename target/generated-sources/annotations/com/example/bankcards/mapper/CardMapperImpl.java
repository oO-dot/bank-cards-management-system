package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-20T03:34:38+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class CardMapperImpl implements CardMapper {

    @Override
    public CardDTO toCardDTO(Card card) {
        if ( card == null ) {
            return null;
        }

        CardDTO.CardDTOBuilder cardDTO = CardDTO.builder();

        cardDTO.userId( cardUserId( card ) );
        cardDTO.owner( card.getOwner() );
        cardDTO.expirationDate( card.getExpirationDate() );
        cardDTO.id( card.getId() );
        cardDTO.status( card.getStatus() );
        cardDTO.balance( card.getBalance() );

        return cardDTO.build();
    }

    @Override
    public Card toEntity(CardDTO cardDTO) {
        if ( cardDTO == null ) {
            return null;
        }

        Card card = new Card();

        card.setId( cardDTO.getId() );
        card.setOwner( cardDTO.getOwner() );
        card.setExpirationDate( cardDTO.getExpirationDate() );

        return card;
    }

    @Override
    public List<CardDTO> toDtoList(List<Card> cards) {
        if ( cards == null ) {
            return null;
        }

        List<CardDTO> list = new ArrayList<CardDTO>( cards.size() );
        for ( Card card : cards ) {
            list.add( toCardDTO( card ) );
        }

        return list;
    }

    @Override
    public List<Card> toEntityList(List<CardDTO> cardDTOs) {
        if ( cardDTOs == null ) {
            return null;
        }

        List<Card> list = new ArrayList<Card>( cardDTOs.size() );
        for ( CardDTO cardDTO : cardDTOs ) {
            list.add( toEntity( cardDTO ) );
        }

        return list;
    }

    private Long cardUserId(Card card) {
        User user = card.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}
