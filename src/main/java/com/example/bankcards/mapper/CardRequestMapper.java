package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardRequestDTO;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "encryptedNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "owner", target = "owner") // Явно маппим owner
    @Mapping(source = "expirationDate", target = "expirationDate") // Явно маппим expirationDate
    //@Mapping(target = "number", ignore = true)
    Card toEntity(CardRequestDTO cardRequestDTO);
    // CardRequestDTO toCardRequestDTO(Card card); // обратное преобразование для запросов, если понадобится, но обычно не нужно

}
