package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO createUser(UserCreateDTO userCreateDTO);
    UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
}
