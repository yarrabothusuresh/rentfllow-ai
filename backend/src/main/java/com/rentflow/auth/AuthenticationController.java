package com.rentflow.auth;

import com.rentflow.auth.dto.AuthResponseDTO;
import com.rentflow.auth.dto.LoginRequestDTO;
import com.rentflow.security.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RateLimitingService rateLimitingService;

    public AuthenticationController(AuthenticationService authenticationService,
                                    RateLimitingService rateLimitingService) {
        this.authenticationService = authenticationService;
        this.rateLimitingService = rateLimitingService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        String clientIp = rateLimitingService.resolveSafeClientIp(httpRequest);

        try {
            AuthResponseDTO response = authenticationService.login(request, clientIp);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "Invalid credentials"
            ));
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", e.getMessage()
            ));
        }
    }
}
