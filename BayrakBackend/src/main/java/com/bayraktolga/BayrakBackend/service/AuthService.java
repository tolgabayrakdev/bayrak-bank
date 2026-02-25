package com.bayraktolga.BayrakBackend.service;

import com.bayraktolga.BayrakBackend.dto.request.LoginRequest;
import com.bayraktolga.BayrakBackend.dto.request.RefreshTokenRequest;
import com.bayraktolga.BayrakBackend.dto.request.RegisterRequest;
import com.bayraktolga.BayrakBackend.dto.response.AuthResponse;
import com.bayraktolga.BayrakBackend.dto.response.UserResponse;
import com.bayraktolga.BayrakBackend.entity.RefreshToken;
import com.bayraktolga.BayrakBackend.entity.User;
import com.bayraktolga.BayrakBackend.enums.NotificationType;
import com.bayraktolga.BayrakBackend.enums.Role;
import com.bayraktolga.BayrakBackend.repository.UserRepository;
import com.bayraktolga.BayrakBackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Bu email adresi zaten kullanılıyor.");
        }
        if (userRepository.existsByTcNo(request.tcNo())) {
            throw new RuntimeException("Bu TC Kimlik No ile zaten bir hesap mevcut.");
        }

        User user = User.builder()
                .tcNo(request.tcNo())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .birthDate(request.birthDate())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        notificationService.createNotification(
                savedUser,
                "Hoş Geldiniz!",
                "Bayrak Bank ailesine hoş geldiniz, " + savedUser.getFirstName() + "!",
                NotificationType.SYSTEM
        );
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());

        return toUserResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token bulunamadı."));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);

        return new AuthResponse(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token bulunamadı."));

        refreshTokenService.revokeAllByUser(refreshToken.getUser());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getTcNo(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getBirthDate(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
