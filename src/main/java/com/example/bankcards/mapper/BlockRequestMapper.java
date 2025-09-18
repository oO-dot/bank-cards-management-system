package com.example.bankcards.mapper;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.entity.BlockRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BlockRequestMapper {

    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user", target = "userFullName", qualifiedByName = "getUserFullName")
    @Mapping(source = "processedBy.id", target = "processedBy")
    @Mapping(target = "maskedCardNumber", ignore = true) // Будем устанавливать вручную в сервисе
    BlockRequestDTO toDto(BlockRequest blockRequest);

    @Named("getUserFullName")
    default String getUserFullName(com.example.bankcards.entity.User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}