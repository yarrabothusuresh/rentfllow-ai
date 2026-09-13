package com.rentflow.auth;

import com.rentflow.auth.dto.AuthResponseDTO;
import com.rentflow.auth.dto.LoginRequestDTO;
import com.rentflow.auth.dto.UserSummaryDTO;
import com.rentflow.role.Role;
import com.rentflow.security.JwtService;
import com.rentflow.security.RateLimitingService;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimitingService rateLimitingService;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 RateLimitingService rateLimitingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rateLimitingService = rateLimitingService;
    }

    public AuthResponseDTO login(LoginRequestDTO request, String clientIp) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String email = request.getEmail().trim().toLowerCase();

        // Rate limiting check per email and per client IP
        String rateLimitKey = "login:" + (clientIp != null ? clientIp : "global") + ":" + email;
        if (!rateLimitingService.tryAcquire(rateLimitKey, 10)) {
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            // Constant-time dummy check to prevent timing attacks
            passwordEncoder.matches("dummy-password", "$2a$12$e80yVvJd2z1yYwQ2r2/aeeFvGgW5KjQ4K4W4P8F2j7.c9oR0F0d.");
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String tenantId = (user.getTenant() != null) ? user.getTenant().getId().toString() : "99999999-9999-9999-9999-999999999999";
        String primaryRole = user.getRoles().stream()
                .map(r -> r.getRoleType().name())
                .findFirst()
                .orElse("STAFF");

        String token = jwtService.generateToken(
                user.getId(),
                tenantId,
                user.getEmail(),
                primaryRole,
                null // Staff user does not have a customerId
        );

        UserSummaryDTO summary = new UserSummaryDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                primaryRole,
                tenantId
        );

        return new AuthResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                summary
        );
    }
}
