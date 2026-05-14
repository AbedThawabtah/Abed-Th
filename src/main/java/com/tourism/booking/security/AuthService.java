package com.tourism.booking.security;



import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.security.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public MeResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername().trim())) {
            throw new BusinessException("Username already taken");
        }
        if (appUserRepository.existsByEmail(request.getEmail().trim())) {
            throw new BusinessException("Email already registered");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();
        user = appUserRepository.save(user);
        return toMe(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException("Refresh token expired");
        }
        AppUser user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return issueTokens(user);
    }

    @Transactional
    public void revoke(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public MeResponse me(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toMe(user);
    }

    private AuthResponse issueTokens(AppUser user) {
        String access = jwtTokenService.createAccessToken(user);
        String refresh = UUID.randomUUID().toString();
        Instant exp = Instant.now().plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS);
        RefreshToken entity = RefreshToken.builder()
                .token(refresh)
                .user(user)
                .expiresAt(exp)
                .build();
        refreshTokenRepository.save(entity);
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .expiresInSeconds(jwtProperties.accessTokenMinutes() * 60)
                .build();
    }

    private MeResponse toMe(AppUser user) {
        return MeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

