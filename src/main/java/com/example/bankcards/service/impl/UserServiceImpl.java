package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserCreateDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserUpdateDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;
    final UserMapper userMapper;
    final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return userMapper.toUserDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {

        List<User> users = userRepository.findAll();
        return userMapper.toUserDTOList(users);
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(userCreateDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setFirstName(userCreateDTO.getFirstName());
        user.setLastName(userCreateDTO.getLastName());
        user.setRole(Role.valueOf(userCreateDTO.getRole().toUpperCase()));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден с id: " + id));

            // Валидация роли
            validateRole(userUpdateDTO.getRole());

            // Обновляем только разрешенные поля
            user.setFirstName(userUpdateDTO.getFirstName());
            user.setLastName(userUpdateDTO.getLastName());
            user.setRole(userUpdateDTO.getRole());

            // Обновляем статус активности, если передан
            if (userUpdateDTO.getEnabled() != null) {
                user.setEnabled(userUpdateDTO.getEnabled());
            }

            User savedUser = userRepository.save(user);
            return userMapper.toUserDTO(savedUser);
        } catch (DataAccessException e) {
            throw new ValidationException("Ошибка доступа к данным при обновлении пользователя", e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {

        return userRepository.existsByUsername(username);
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new ValidationException("Роль не может быть null");
        }

        // Явная проверка допустимых значений роли
        if (role != Role.USER && role != Role.ADMIN) {
            throw new ValidationException("Неверная роль: " + role + ". Допустимые значения: USER, ADMIN");
        }
    }

}

