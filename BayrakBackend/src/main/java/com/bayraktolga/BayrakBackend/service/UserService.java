package com.bayraktolga.BayrakBackend.service;

import com.bayraktolga.BayrakBackend.dto.request.ChangePasswordRequest;
import com.bayraktolga.BayrakBackend.dto.request.UpdateProfileRequest;
import com.bayraktolga.BayrakBackend.dto.response.UserResponse;
import com.bayraktolga.BayrakBackend.entity.User;
import com.bayraktolga.BayrakBackend.enums.NotificationType;
import com.bayraktolga.BayrakBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return toUserResponse(findUserById(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName()  != null) user.setLastName(request.lastName());
        if (request.phone()     != null) user.setPhone(request.phone());
        if (request.birthDate() != null) user.setBirthDate(request.birthDate());

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new RuntimeException("Bu email adresi zaten kullanılıyor.");
            }
            user.setEmail(request.email());
        }

        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mevcut şifre hatalı.");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RuntimeException("Yeni şifreler eşleşmiyor.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        notificationService.publishNotification(
                user.getId(),
                "Şifre Değiştirildi",
                "Hesabınızın şifresi başarıyla güncellendi.",
                NotificationType.SECURITY
        );
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
    }

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
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
