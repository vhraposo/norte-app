package com.norte.service;

import com.norte.dto.AuthResponse;
import com.norte.dto.LoginRequest;
import com.norte.dto.RegisterRequest;
import com.norte.entity.AuthToken;
import com.norte.entity.User;
import com.norte.exception.ApiException;
import com.norte.repository.AuthTokenRepository;
import com.norte.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, AuthTokenRepository authTokenRepository) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ja existe uma conta com esse e-mail");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        userRepository.save(user);

        String token = issueToken(user);
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos");
        }

        String token = issueToken(user);
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    private String issueToken(User user) {
        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setToken(UUID.randomUUID().toString());
        authToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        authTokenRepository.save(authToken);
        return authToken.getToken();
    }
}
