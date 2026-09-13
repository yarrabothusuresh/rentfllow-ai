package com.rentflow.security;

import com.rentflow.auth.AuthenticationService;
import com.rentflow.auth.RateLimitExceededException;
import com.rentflow.auth.dto.AuthResponseDTO;
import com.rentflow.auth.dto.LoginRequestDTO;
import com.rentflow.role.Role;
import com.rentflow.role.RoleType;
import com.rentflow.tenant.Tenant;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private RateLimitingService rateLimitingService;
    private AuthenticationService authService;

    private User activeUser;
    private String rawPassword = "ValidPassword123!";

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(10);

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret("test-secret-key-must-be-at-least-32-bytes-long-for-hmac-sha256!");
        jwtService = new JwtService(jwtProps);

        rateLimitingService = new RateLimitingService();
        rateLimitingService.clearAll();

        authService = new AuthenticationService(userRepository, passwordEncoder, jwtService, rateLimitingService);

        Tenant tenant = new Tenant(UUID.randomUUID(), "Test Tenant");
        Role role = new Role(UUID.randomUUID(), RoleType.OWNER, Set.of());

        activeUser = new User(
                UUID.randomUUID(),
                "John Doe",
                "john@example.com",
                passwordEncoder.encode(rawPassword),
                true,
                tenant,
                Set.of(role)
        );
    }

    @Test
    @DisplayName("Login succeeds with valid credentials")
    void testLoginSuccess() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(activeUser));

        AuthResponseDTO response = authService.login(new LoginRequestDTO("john@example.com", rawPassword), "127.0.0.1");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john@example.com", response.getUser().getEmail());
        assertEquals("OWNER", response.getUser().getRole());
    }

    @Test
    @DisplayName("Login fails with non-existent email")
    void testLoginNonExistentEmail() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequestDTO("unknown@example.com", rawPassword), "127.0.0.1")
        );
    }

    @Test
    @DisplayName("Login fails with incorrect password")
    void testLoginIncorrectPassword() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(activeUser));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequestDTO("john@example.com", "WrongPassword!"), "127.0.0.1")
        );
    }

    @Test
    @DisplayName("Login fails for inactive/disabled user")
    void testLoginInactiveUser() {
        activeUser.setActive(false);
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(activeUser));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequestDTO("john@example.com", rawPassword), "127.0.0.1")
        );
    }

    @Test
    @DisplayName("Login rate limits after consecutive attempts")
    void testLoginRateLimiting() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(activeUser));

        // 10 allowed attempts
        for (int i = 0; i < 10; i++) {
            authService.login(new LoginRequestDTO("john@example.com", rawPassword), "192.168.1.100");
        }

        // 11th attempt must be rejected with RateLimitExceededException
        assertThrows(RateLimitExceededException.class, () ->
                authService.login(new LoginRequestDTO("john@example.com", rawPassword), "192.168.1.100")
        );
    }
}
