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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("User Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should return true when username exists in database")
    void existsByUsername_WhenUsernameExists_ShouldReturnTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        boolean result = userService.existsByUsername("testuser");
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when username does not exist in database")
    void existsByUsername_WhenUserNotExists_ShouldReturnFalse() {
        when(userRepository.existsByUsername("unknown")).thenReturn(false);
        boolean result = userService.existsByUsername("unknown");
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return correct results for different usernames")
    void existsByUsername_WithDifferentUsernames_ShouldReturnCorrectResults() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        assertTrue(userService.existsByUsername("existing"));
        assertFalse(userService.existsByUsername("nonexistent"));

        verify(userRepository, times(1)).existsByUsername("existing");
        verify(userRepository, times(1)).existsByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should delete user when admin and user exists")
    void deleteUser_WhenUserExists_ShouldCallRepository() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            when(userRepository.existsById(1L)).thenReturn(true);
            userService.deleteUser(1L);
            verify(userRepository, times(1)).existsById(1L);
            verify(userRepository, times(1)).deleteById(1L);
        }
    }

    @Test
    @DisplayName("Should throw exception when admin tries to delete non-existent user")
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            when(userRepository.existsById(999L)).thenReturn(false);

            assertThrows(
                    org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                    () -> userService.deleteUser(999L)
            );

            verify(userRepository, times(1)).existsById(999L);
            verify(userRepository, never()).deleteById(999L);
        }
    }

    @Test
    @DisplayName("Should throw access denied when non-admin tries to delete user")
    void deleteUser_WhenNotAdmin_ShouldThrowAccessDenied() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            assertThrows(
                    org.springframework.security.access.AccessDeniedException.class,
                    () -> userService.deleteUser(1L)
            );

            verify(userRepository, never()).deleteById(1L);
            verify(userRepository, never()).existsById(1L);
        }
    }

    @Test
    @DisplayName("Should throw exception when unauthenticated user tries to delete")
    void deleteUser_WhenNotAuthenticated_ShouldThrowException() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            SecurityContext securityContext = mock(SecurityContext.class);
            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            assertThrows(
                    org.springframework.security.access.AccessDeniedException.class,
                    () -> userService.deleteUser(1L)
            );

            verify(userRepository, never()).existsById(any());
            verify(userRepository, never()).deleteById(any());
        }
    }

    // 🔹 getUserById tests
    @Test
    @DisplayName("Should return user DTO when admin requests any user by ID")
    void getUserById_WhenAdminAndUserExists_ShouldReturnUserDTO() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            User testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");

            UserDTO expectedDTO = new UserDTO();
            expectedDTO.setId(1L);
            expectedDTO.setUsername("testuser");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserDTO(testUser)).thenReturn(expectedDTO);

            UserDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository, times(1)).findById(1L);
            verify(userMapper, times(1)).toUserDTO(testUser);
        }
    }

    @Test
    @DisplayName("Should return user DTO when user requests own data by ID")
    void getUserById_WhenUserRequestsOwnData_ShouldReturnUserDTO() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            when(authentication.getName()).thenReturn("testuser");

            User testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");

            UserDTO expectedDTO = new UserDTO();
            expectedDTO.setId(1L);
            expectedDTO.setUsername("testuser");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserDTO(testUser)).thenReturn(expectedDTO);

            UserDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository, times(1)).findById(1L);
        }
    }

    @Test
    @DisplayName("Should throw access denied when user requests other user's data")
    void getUserById_WhenUserRequestsOtherUserData_ShouldThrowAccessDenied() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            when(authentication.getName()).thenReturn("otheruser");

            User testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThrows(
                    org.springframework.security.access.AccessDeniedException.class,
                    () -> userService.getUserById(1L)
            );

            verify(userRepository, times(1)).findById(1L);
            verify(userMapper, never()).toUserDTO(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                    () -> userService.getUserById(999L)
            );

            verify(userRepository, times(1)).findById(999L);
            verify(userMapper, never()).toUserDTO(any());
        }
    }

    // 🔹 getAllUsers tests
    @Test
    @DisplayName("Should return list of all users when admin requests")
    void getAllUsers_WhenAdmin_ShouldReturnUserList() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            User user1 = new User();
            user1.setId(1L);
            User user2 = new User();
            user2.setId(2L);
            List<User> users = Arrays.asList(user1, user2);

            UserDTO dto1 = new UserDTO();
            dto1.setId(1L);
            UserDTO dto2 = new UserDTO();
            dto2.setId(2L);
            List<UserDTO> expectedDTOs = Arrays.asList(dto1, dto2);

            when(userRepository.findAll()).thenReturn(users);
            when(userMapper.toUserDTOList(users)).thenReturn(expectedDTOs);

            List<UserDTO> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userRepository, times(1)).findAll();
            verify(userMapper, times(1)).toUserDTOList(users);
        }
    }

    @Test
    @DisplayName("Should throw access denied when non-admin requests all users")
    void getAllUsers_WhenNotAdmin_ShouldThrowAccessDenied() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            assertThrows(
                    org.springframework.security.access.AccessDeniedException.class,
                    () -> userService.getAllUsers()
            );

            verify(userRepository, never()).findAll();
        }
    }

    // 🔹 createUser tests
    @Test
    @DisplayName("Should create user and return DTO when admin provides valid data")
    void createUser_WhenAdminAndValidData_ShouldReturnUserDTO() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("newuser");
            createDTO.setPassword("password");
            createDTO.setFirstName("John");
            createDTO.setLastName("Doe");
            createDTO.setRole("USER");

            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername("newuser");

            UserDTO expectedDTO = new UserDTO();
            expectedDTO.setId(1L);
            expectedDTO.setUsername("newuser");

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toUserDTO(savedUser)).thenReturn(expectedDTO);

            UserDTO result = userService.createUser(createDTO);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository, times(1)).existsByUsername("newuser");
            verify(passwordEncoder, times(1)).encode("password");
            verify(userRepository, times(1)).save(any(User.class));
            verify(userMapper, times(1)).toUserDTO(savedUser);
        }
    }

    @Test
    @DisplayName("Should throw exception when admin tries to create user with duplicate username")
    void createUser_WhenDuplicateUsername_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("existinguser");

            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.createUser(createDTO)
            );

            assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    @DisplayName("Should throw access denied when non-admin tries to create user")
    void createUser_WhenNotAdmin_ShouldThrowAccessDenied() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            UserCreateDTO createDTO = new UserCreateDTO();

            assertThrows(
                    org.springframework.security.access.AccessDeniedException.class,
                    () -> userService.createUser(createDTO)
            );

            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    // 🔹 updateUser tests
    @Test
    @DisplayName("Should update user and return DTO when admin provides valid data")
    void updateUser_WhenAdminAndValidData_ShouldReturnUpdatedUserDTO() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            User existingUser = new User();
            existingUser.setId(1L);
            existingUser.setFirstName("OldName");

            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setFirstName("NewName");
            updateDTO.setLastName("NewLastName");
            updateDTO.setRole(Role.USER);

            User updatedUser = new User();
            updatedUser.setId(1L);
            updatedUser.setFirstName("NewName");

            UserDTO expectedDTO = new UserDTO();
            expectedDTO.setId(1L);
            expectedDTO.setFirstName("NewName");

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.toUserDTO(updatedUser)).thenReturn(expectedDTO);

            UserDTO result = userService.updateUser(1L, updateDTO);

            assertNotNull(result);
            assertEquals("NewName", result.getFirstName());
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).save(any(User.class));
            verify(userMapper, times(1)).toUserDTO(updatedUser);
        }
    }

    @Test
    @DisplayName("Should throw exception when admin tries to update non-existent user")
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setRole(Role.USER);

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    NotFoundException.class,
                    () -> userService.updateUser(999L, updateDTO)
            );

            verify(userRepository, times(1)).findById(999L);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    @DisplayName("Should throw validation exception when admin provides invalid role")
    void updateUser_WhenInvalidRole_ShouldThrowValidationException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            User existingUser = new User();
            existingUser.setId(1L);

            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setRole(null); // Invalid role

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            assertThrows(
                    ValidationException.class,
                    () -> userService.updateUser(1L, updateDTO)
            );

            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    // 🔹 getCurrentUserId tests
    @Test
    @DisplayName("Should return current user ID when user is authenticated")
    void getCurrentUserId_WhenAuthenticated_ShouldReturnUserId() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            // 🔹 ТОЛЬКО getName - больше ничего не нужно!
            when(authentication.getName()).thenReturn("testuser");

            User testUser = new User();
            testUser.setId(1L);

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            Long result = userService.getCurrentUserId();

            assertEquals(1L, result);
            verify(userRepository, times(1)).findByUsername("testuser");
        }
    }

    // 🔹 isCurrentUserAdmin tests
    @Test
    @DisplayName("Should return true when current user has admin role")
    void isCurrentUserAdmin_WhenUserIsAdmin_ShouldReturnTrue() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            // 🔹 ТОЛЬКО getName - больше ничего не нужно!
            when(authentication.getName()).thenReturn("admin");

            User adminUser = new User();
            adminUser.setId(1L);
            adminUser.setRole(Role.ADMIN);

            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            boolean result = userService.isCurrentUserAdmin();

            assertTrue(result);
            verify(userRepository, times(1)).findByUsername("admin");
        }
    }

    @Test
    @DisplayName("Should return false when current user does not have admin role")
    void isCurrentUserAdmin_WhenUserIsNotAdmin_ShouldReturnFalse() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            // 🔹 ТОЛЬКО getName - больше ничего не нужно!
            when(authentication.getName()).thenReturn("user");

            User regularUser = new User();
            regularUser.setId(1L);
            regularUser.setRole(Role.USER);

            when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));

            boolean result = userService.isCurrentUserAdmin();

            assertFalse(result);
            verify(userRepository, times(1)).findByUsername("user");
        }
    }

    // 🔹 getCurrentUser tests
    @Test
    @DisplayName("Should return current user DTO when user is authenticated")
    void getCurrentUser_WhenAuthenticated_ShouldReturnUserDTO() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            // 🔹 ТОЛЬКО getName - больше ничего не нужно!
            when(authentication.getName()).thenReturn("testuser");

            User testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");

            UserDTO expectedDTO = new UserDTO();
            expectedDTO.setId(1L);
            expectedDTO.setUsername("testuser");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userMapper.toUserDTO(testUser)).thenReturn(expectedDTO);

            UserDTO result = userService.getCurrentUser();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            verify(userRepository, times(1)).findByUsername("testuser");
            verify(userMapper, times(1)).toUserDTO(testUser);
        }
    }

    @Test
    @DisplayName("Should throw exception when current user not found in database")
    void getCurrentUser_WhenUserNotFound_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {

            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            // 🔹 ТОЛЬКО getName - больше ничего не нужно!
            when(authentication.getName()).thenReturn("unknown");

            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThrows(
                    org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                    () -> userService.getCurrentUser()
            );

            verify(userRepository, times(1)).findByUsername("unknown");
            verify(userMapper, never()).toUserDTO(any());
        }
    }
}