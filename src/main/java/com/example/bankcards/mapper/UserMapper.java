package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDTO toUserDTO(User user);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toEntity(UserDTO userDTO);

    List<UserDTO> toUserDTOList(List<User> users);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "cards", ignore = true)
    List<User> toEntityList(List<UserDTO> userDTOs);

}
