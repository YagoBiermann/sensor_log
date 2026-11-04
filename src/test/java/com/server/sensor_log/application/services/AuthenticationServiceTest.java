package com.server.sensor_log.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.vault.core.VaultTemplate;

import com.server.sensor_log.application.controllers.user.dto.LoginRequest;
import com.server.sensor_log.application.controllers.user.dto.RegisterRequest;
import com.server.sensor_log.application.exceptions.InvalidCredentialsException;
import com.server.sensor_log.application.exceptions.UserAlreadyExistsException;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.user.User;
import com.server.sensor_log.infra.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock VaultTemplate vaultTemplate;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RandomGenerator randomGenerator;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(vaultTemplate, userRepository, passwordEncoder, jwtService, randomGenerator);
    }

    @Test
    void registersUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password", "User");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(randomGenerator.generateUserId()).thenReturn("id-1");
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        service.register(request);

        verify(userRepository).save(argThat(user -> user.getUserId().equals("id-1")
                && user.getEmail().equals(request.email())
                && user.getPassword().equals("encoded")));
    }

    @Test
    void rejectsDuplicateUser() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> service.register(new RegisterRequest("user@example.com", "password", null)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownUserAndWrongPassword() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(null);
        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("unknown@example.com", "password")));

        User user = new User(randomGenerator, "user@example.com", "encoded");
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("user@example.com", "wrong")));
    }

    @Test
    void returnsJwtForValidCredentials() {
        when(randomGenerator.generateUserId()).thenReturn("user-id");
        User user = new User(randomGenerator, "user@example.com", "encoded");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtService.generateToken(anyString(), eq("USER"))).thenReturn("jwt");

        assertEquals("jwt", service.login(new LoginRequest(user.getEmail(), "password")).token());
        verify(jwtService).generateToken(user.getUserId(), "USER");
    }
}
