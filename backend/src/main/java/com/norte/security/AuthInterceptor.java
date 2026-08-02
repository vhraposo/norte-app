package com.norte.security;

import com.norte.entity.AuthToken;
import com.norte.entity.User;
import com.norte.exception.ApiException;
import com.norte.repository.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenRepository authTokenRepository;

    // Rotas publicas que nao exigem token
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/register",
        "/api/auth/login",
        "/h2-console"
    };

    public AuthInterceptor(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Requisicoes de preflight do CORS (OPTIONS) nao enviam o header Authorization.
        // Elas precisam passar sem exigir token, ou o navegador bloqueia a requisicao real.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p)) return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de autenticacao ausente");
        }

        String token = header.substring(7);
        Optional<AuthToken> authTokenOpt = authTokenRepository.findByToken(token);
        if (authTokenOpt.isEmpty() || authTokenOpt.get().getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token invalido ou expirado");
        }

        User user = authTokenOpt.get().getUser();
        CurrentUserHolder.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserHolder.clear();
    }
}
