package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cardNumber", ignore = true) // Игнорируем, будем устанавливать вручную
    @Mapping(source = "user.id", target = "userId") // Автоматическое преобразование связи
    @Mapping(source = "owner", target = "owner") // Явно указываем маппинг
    @Mapping(source = "expirationDate", target = "expirationDate") // Явно указываем маппинг
    CardDTO toCardDTO(Card card);

    @Mapping(target = "encryptedNumber", ignore = true) // Игнорируем при обратном преобразовании
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "balance", ignore = true)
    Card toEntity(CardDTO cardDTO);

    List<CardDTO> toDtoList(List<Card> cards);
    List<Card> toEntityList(List<CardDTO> cardDTOs);

}
